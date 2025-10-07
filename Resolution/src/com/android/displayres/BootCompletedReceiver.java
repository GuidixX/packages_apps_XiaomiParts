/*
* Copyright (C) GuidixX
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
*
*/

package com.android.displayres;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.preference.PreferenceManager;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "CustomDisplayResolution";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            handleBootCompleted(context);
        }
    }

    private void handleBootCompleted(Context context) {
        try {
            // Check if service is enabled
            boolean serviceEnabled = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("service_enabled", false);
            
            if (!serviceEnabled) {
                Log.d(TAG, "Resolution service is disabled, skipping boot restoration");
                return;
            }
            
            String res = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(Constants.PREF_KEY_RES_VALUE, null);
            String density = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(Constants.PREF_KEY_DPI_VALUE, null);
            if (res != null && density != null && !res.isEmpty() && !density.isEmpty()) {
                ShellUtils.applyResolutionAndDensity(res, density);
                Log.d(TAG, "Restored resolution " + res + " density " + density);
            } else {
                // Save defaults from current physical values
                String curRes = ShellUtils.getCurrentSize();
                String curDensity = ShellUtils.getCurrentDensity();
                if (curRes != null && curDensity != null) {
                    PreferenceManager.getDefaultSharedPreferences(context)
                        .edit().putString(Constants.PREF_KEY_DEFAULT_RES, curRes)
                        .putString(Constants.PREF_KEY_DEFAULT_DPI, curDensity).apply();
                    Log.d(TAG, "Saved default resolution " + curRes + " density " + curDensity);
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to restore resolution", t);
        }
    }
}
