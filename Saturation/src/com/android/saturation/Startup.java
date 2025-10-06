/*
 * Copyright (C) 2024 The LineageOS Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import androidx.preference.PreferenceManager;

import com.android.saturation.Constants;
import com.android.saturation.SaturationFragment;
import com.android.saturation.utils.ComponentUtils;
import com.android.saturation.utils.FileUtils;

public class Startup extends BroadcastReceiver {

    private static final String TAG = "Startup";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "onReceive called with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            Intent.ACTION_REBOOT.equals(action)) {

            // Adding a delay before applying the saturation
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "Applying saved saturation setting...");
                applySavedSaturation(context);
                // Start per-app monitor if enabled
                try {
                    boolean perApp = PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(Constants.KEY_PER_APP_ENABLED, false);
                    if (perApp) {
                        Intent svc = new Intent();
                        svc.setClassName(context, "com.android.saturation.SaturationPerAppService");
                        context.startService(svc);
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Failed to start per-app service", t);
                }
            }, 5000); // Delay of 5 seconds
        }
    }

    private void applySavedSaturation(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int seekBarValue = sharedPrefs.getInt(Constants.KEY_SATURATION, 100);
        boolean perAppEnabled = sharedPrefs.getBoolean(Constants.KEY_PER_APP_ENABLED, false);
        Log.d(TAG, "Retrieved seekBarValue: " + seekBarValue + ", perAppEnabled: " + perAppEnabled);

        // Only apply saturation directly if per-app mode is disabled
        if (!perAppEnabled) {
            applySaturation(seekBarValue);
        } else {
            Log.d(TAG, "Per-app mode enabled, skipping direct saturation application");
        }
    }

    private void applySaturation(int seekBarValue) {
        Log.d(TAG, "Applying saturation: " + seekBarValue);

        float saturation;
        if (seekBarValue == 100) {
            saturation = 1.001f;
        } else {
            saturation = seekBarValue / 100.0f;
        }

        IBinder surfaceFlinger = ServiceManager.getService("SurfaceFlinger");
        if (surfaceFlinger != null) {
            try {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeFloat(saturation);
                surfaceFlinger.transact(1022, data, null, 0);
                data.recycle();
                Log.d(TAG, "Saturation applied successfully");
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to apply saturation", e);
            }
        } else {
            Log.e(TAG, "SurfaceFlinger service not found");
        }
    }
}
