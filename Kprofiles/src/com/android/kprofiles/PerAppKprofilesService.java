/*
 * Copyright (C) 2026 GuidixX
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.kprofiles;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.app.Service;
import android.app.TaskStackListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.android.kprofiles.utils.FileUtils;

import java.util.List;

public class PerAppKprofilesService extends Service {

    private static final String TAG = "PerAppKprofilesService";
    private static final boolean DEBUG = false;

    // Task stack changes arrive in bursts during a single app switch; coalesce
    // them so we only apply the settled foreground app once.
    private static final long APPLY_DEBOUNCE_MS = 150L;

    private KprofilesUtils mKprofilesUtils;
    private PowerManager mPowerManager;
    private boolean mTaskListenerRegistered;
    private volatile boolean mStopping;

    // Dedicated worker so kernel-node writes (which can block) never run on the
    // binder thread that delivers task-stack callbacks. All mutable state below
    // is touched only from this thread, so no extra synchronization is needed.
    private HandlerThread mWorkerThread;
    private Handler mWorker;
    private String mPreviousApp;
    private boolean mInteractive;

    private final Runnable mApplyRunnable = this::applyForegroundApp;
    private final Runnable mSyncScreenRunnable = this::syncScreenState;

    private final TaskStackListener mTaskListener = new TaskStackListener() {
        @Override
        public void onTaskStackChanged() {
            scheduleApply();
        }
    };

    private final BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)
                    || Intent.ACTION_SCREEN_ON.equals(action)) {
                mWorker.removeCallbacks(mSyncScreenRunnable);
                mWorker.post(mSyncScreenRunnable);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "Creating service");
        mKprofilesUtils = new KprofilesUtils(this);
        mPowerManager = getSystemService(PowerManager.class);

        mWorkerThread = new HandlerThread("kprofiles-worker");
        mWorkerThread.start();
        mWorker = new Handler(mWorkerThread.getLooper());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mScreenReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        // Screen tracking is always needed on new kernels. Task tracking is
        // only useful when the user has actually assigned per-app profiles.
        updateTaskStackListener(KprofilesUtils.hasPerAppProfiles(this));
        mWorker.removeCallbacks(mSyncScreenRunnable);
        mWorker.post(mSyncScreenRunnable);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mStopping = true;
        try {
            unregisterReceiver(mScreenReceiver);
        } catch (Exception e) {
            // ignore if never registered
        }
        updateTaskStackListener(false);
        // Restore the global profile on the worker, then tear it down so the
        // final write is not lost to a premature quit().
        mWorker.removeCallbacks(mApplyRunnable);
        mWorker.removeCallbacks(mSyncScreenRunnable);
        mWorker.post(() -> {
            mKprofilesUtils.setDefaultProfile();
            mWorkerThread.quitSafely();
        });
        super.onDestroy();
    }

    private void updateTaskStackListener(boolean enabled) {
        if (enabled == mTaskListenerRegistered) return;
        try {
            IActivityTaskManager atm = ActivityTaskManager.getService();
            if (enabled) {
                atm.registerTaskStackListener(mTaskListener);
            } else {
                atm.unregisterTaskStackListener(mTaskListener);
            }
            mTaskListenerRegistered = enabled;
        } catch (Exception e) {
            Log.e(TAG, "Failed to update task stack listener", e);
        }
    }

    /** Runs on the worker, sampling current state instead of replaying stale intents. */
    private void syncScreenState() {
        if (mStopping || mPowerManager == null) return;
        mInteractive = mPowerManager.isInteractive();
        String value = mInteractive ? "1" : "0";
        // Older kernels lack this node. Power HAL may already have updated it;
        // avoid a duplicate write (and the kernel's synchronous QoS work).
        if (FileUtils.fileExists(Constants.KPROFILES_SCREEN_ON_NODE)
                && !value.equals(FileUtils.readOneLine(Constants.KPROFILES_SCREEN_ON_NODE))) {
            FileUtils.writeLine(Constants.KPROFILES_SCREEN_ON_NODE, value);
        }

        mWorker.removeCallbacks(mApplyRunnable);
        mPreviousApp = null;
        if (mInteractive && KprofilesUtils.hasPerAppProfiles(this)) {
            scheduleApply();
        } else {
            // Restore the selected global mode; auto_kp in the kernel decides
            // whether an inactive display temporarily means Battery.
            mKprofilesUtils.setDefaultProfile();
        }
    }

    /** Coalesce rapid callbacks and hand the work to the worker thread. */
    private void scheduleApply() {
        if (mStopping) return;
        mWorker.removeCallbacks(mApplyRunnable);
        mWorker.postDelayed(mApplyRunnable, APPLY_DEBOUNCE_MS);
    }

    /** Runs on the worker thread only. */
    private void applyForegroundApp() {
        // Task callbacks can arrive after SCREEN_OFF or before its broadcast.
        if (mStopping || !mInteractive || mPowerManager == null
                || !mPowerManager.isInteractive()) return;
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) return;
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks == null || tasks.isEmpty()) return;

            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity == null) return;

            String foregroundApp = topActivity.getPackageName();
            if (foregroundApp.equals(mPreviousApp)) return;

            // setKprofilesProfile() applies the per-app override if one exists,
            // otherwise it restores the global default. That single call keeps
            // both cases correct without tracking saved/overridden state here.
            mKprofilesUtils.setKprofilesProfile(foregroundApp);
            mPreviousApp = foregroundApp;
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error applying foreground app profile", e);
        }
    }
}
