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
import android.util.Log;

import java.util.List;

public class PerAppKprofilesService extends Service {

    private static final String TAG = "PerAppKprofilesService";
    private static final boolean DEBUG = false;

    // Task stack changes arrive in bursts during a single app switch; coalesce
    // them so we only apply the settled foreground app once.
    private static final long APPLY_DEBOUNCE_MS = 150L;

    private KprofilesUtils mKprofilesUtils;

    // Dedicated worker so kernel-node writes (which can block) never run on the
    // binder thread that delivers task-stack callbacks. All mutable state below
    // is touched only from this thread, so no extra synchronization is needed.
    private HandlerThread mWorkerThread;
    private Handler mWorker;
    private String mPreviousApp;

    private final Runnable mApplyRunnable = this::applyForegroundApp;

    private final BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // Drop back to the global profile while the screen is off so an
                // override can't stay latched on an idle device.
                mWorker.post(() -> {
                    mPreviousApp = null;
                    mKprofilesUtils.setDefaultProfile();
                });
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // Re-apply for whatever is in the foreground on wake.
                scheduleApply();
            }
        }
    };

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        mKprofilesUtils = new KprofilesUtils(this);

        mWorkerThread = new HandlerThread("kprofiles-worker");
        mWorkerThread.start();
        mWorker = new Handler(mWorkerThread.getLooper());

        registerTaskStackListener();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mScreenReceiver, filter);

        // Apply for the current foreground app right away.
        scheduleApply();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(mScreenReceiver);
        } catch (Exception e) {
            // ignore if never registered
        }
        // Restore the global profile on the worker, then tear it down so the
        // final write is not lost to a premature quit().
        mWorker.removeCallbacks(mApplyRunnable);
        mWorker.post(() -> {
            mKprofilesUtils.setDefaultProfile();
            mWorkerThread.quitSafely();
        });
        super.onDestroy();
    }

    private void registerTaskStackListener() {
        TaskStackListener taskListener = new TaskStackListener() {
            @Override
            public void onTaskStackChanged() {
                scheduleApply();
            }
        };

        try {
            IActivityTaskManager atm = ActivityTaskManager.getService();
            atm.registerTaskStackListener(taskListener);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register task stack listener", e);
        }
    }

    /** Coalesce rapid callbacks and hand the work to the worker thread. */
    private void scheduleApply() {
        mWorker.removeCallbacks(mApplyRunnable);
        mWorker.postDelayed(mApplyRunnable, APPLY_DEBOUNCE_MS);
    }

    /** Runs on the worker thread only. */
    private void applyForegroundApp() {
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
