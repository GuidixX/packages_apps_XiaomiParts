/*
 * Copyright (C) 2023 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.panelorientation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.UserHandle;
import android.os.Looper;
import android.util.Log;
import android.view.Display;

import androidx.preference.PreferenceManager;

import com.android.panelorientation.touch.TouchOrientationService;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "XiaomiParts";
    private static final boolean DEBUG = true;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            return;
        }
        if (DEBUG) Log.d(TAG, "Received boot completed intent");

        try {
            if (DEBUG) Log.d(TAG, "Starting TouchOrientationService");
            // Touchscreen
            context.startServiceAsUser(new Intent(context, TouchOrientationService.class),
                    UserHandle.CURRENT);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start TouchOrientationService", e);
        }
    }
}
