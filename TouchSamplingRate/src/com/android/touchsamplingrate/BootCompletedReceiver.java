/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
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

package com.android.touchsamplingrate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.touchsamplingrate.utils.FileUtils;

import androidx.preference.PreferenceManager;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "Touch Sampling Rate";
    private static final boolean DEBUG = true;
    private static final String TOUCH_SAMPLING_RATE_ENABLE_KEY = "touch_sampling_rate_enable";
    private static final String TOUCH_SAMPLING_RATE_NODE = "/sys/devices/virtual/touch/touch_dev/bump_sample_rate";

    private SharedPreferences sharedPrefs;

    @Override
    public void onReceive(final Context context, Intent intent) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (DEBUG) Log.i(TAG, "Received intent: " + intent.getAction());
        switch (intent.getAction()) {
            case Intent.ACTION_LOCKED_BOOT_COMPLETED:
                handleLockedBootCompleted(context);
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                handleBootCompleted(context);
                break;
        }
    }

    private void handleLockedBootCompleted(Context context) {
        if (DEBUG) Log.i(TAG, "Handling locked boot completed.");
        try {
            // Start necessary services
            startServices(context);

        } catch (Exception e) {
            Log.e(TAG, "Error during locked boot completed", e);
        }
    }

    private void startServices(Context context) {
        if (DEBUG) Log.i(TAG, "Starting services...");

        // High Touch Sampling Rate
        boolean touchSamplingRateEnabled = sharedPrefs.getBoolean(TOUCH_SAMPLING_RATE_ENABLE_KEY, false);
        FileUtils.writeLine(TOUCH_SAMPLING_RATE_NODE, touchSamplingRateEnabled ? "1" : "0");
    }

    private void handleBootCompleted(Context context) {
        if (DEBUG) Log.i(TAG, "Handling boot completed.");
        try {
            startServices(context);
        } catch (Exception e) {
            Log.e(TAG, "Error during boot completed", e);
        }
    }
}
