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
import com.android.panelorientation.touch.TouchUtils;

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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isEdgeRejectionEnabled = prefs.getBoolean(Constants.KEY_EDGE_REJECTION, true);
            if (DEBUG) Log.d(TAG, "Setting initial edge rejection state to: " + isEdgeRejectionEnabled);
            TouchUtils.setEdgeRejectionEnabled(isEdgeRejectionEnabled);

            if (isEdgeRejectionEnabled) {
                if (DEBUG) Log.d(TAG, "Edge rejection is enabled, starting TouchOrientationService.");
                context.startServiceAsUser(new Intent(context, TouchOrientationService.class), UserHandle.CURRENT);
            } else {
                if (DEBUG) Log.d(TAG, "Edge rejection is disabled, TouchOrientationService will not be started.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to set initial edge rejection state or start service", e);
        }
    }
}
