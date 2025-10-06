/*
 * Copyright (C) 2025 GuidixX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.saturation;

import android.app.ActivityTaskManager;
import android.app.Service;
import android.app.TaskStackListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class SaturationPerAppService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SaturationPerApp";

    private SharedPreferences mPrefs;
    private int mSavedSeekBarValue = 100;
    private String mCurrentTopPkg = "";
    private boolean mPerAppEnabled = false;
    private Set<String> mDisabledPackages = new HashSet<>();

    private final TaskStackListener mTaskListener = new TaskStackListener() {
        @Override
        public void onTaskStackChanged() {
            try {
                final ActivityTaskManager.RootTaskInfo focused = ActivityTaskManager.getService().getFocusedRootTaskInfo();
                if (focused != null && focused.topActivity != null) {
                    final String pkg = focused.topActivity.getPackageName();
                    if (!pkg.equals(mCurrentTopPkg)) {
                        mCurrentTopPkg = pkg;
                        applyForTopApp();
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "onTaskStackChanged error", t);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        reloadPrefs();
        try {
            ActivityTaskManager.getService().registerTaskStackListener(mTaskListener);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register TaskStackListener", e);
        }
        // Initial apply
        applyForTopApp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            ActivityTaskManager.getService().unregisterTaskStackListener(mTaskListener);
        } catch (RemoteException e) {
            // ignore
        }
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void reloadPrefs() {
        mSavedSeekBarValue = mPrefs.getInt(Constants.KEY_SATURATION, 100);
        mPerAppEnabled = mPrefs.getBoolean(Constants.KEY_PER_APP_ENABLED, false);
        Set<String> set = mPrefs.getStringSet(Constants.KEY_PER_APP_DISABLED_PACKAGES, null);
        mDisabledPackages = (set == null) ? new HashSet<>() : new HashSet<>(set);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Constants.KEY_SATURATION.equals(key)
                || Constants.KEY_PER_APP_ENABLED.equals(key)
                || Constants.KEY_PER_APP_DISABLED_PACKAGES.equals(key)) {
            reloadPrefs();
            applyForTopApp();
        }
    }

    private void applyForTopApp() {
        if (!mPerAppEnabled) {
            applySaved();
            return;
        }
        boolean enabledForApp = !mDisabledPackages.contains(mCurrentTopPkg);
        if (enabledForApp) {
            applySaved();
        } else {
            applyNeutral();
        }
    }

    private void applySaved() {
        float value = (mSavedSeekBarValue == 100) ? 1.001f : (mSavedSeekBarValue / 100.0f);
        applySaturation(value);
    }

    private void applyNeutral() {
        applySaturation(1.001f);
    }

    private void applySaturation(float saturation) {
        IBinder surfaceFlinger = ServiceManager.getService("SurfaceFlinger");
        if (surfaceFlinger != null) {
            try {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeFloat(saturation);
                surfaceFlinger.transact(1022, data, null, 0);
                data.recycle();
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to apply saturation", e);
            }
        }
    }
}

