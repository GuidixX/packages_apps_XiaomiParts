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

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;

public class ShellUtils {
    private static final String TAG = "ShellUtils";

    public static void applyResolutionAndDensity(String resolution, String density) {
        // Fallback-safe: do not require privileged permissions, rely on shell context
        runQuiet("wm size " + ("reset".equals(resolution) ? "reset" : resolution));
        runQuiet("wm density " + ("reset".equals(density) ? "reset" : density));
        Log.d(TAG, "Applied resolution " + resolution + " density " + density);
    }

    private static void runQuiet(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            // Consume streams to avoid blocking
            new Thread(() -> {
                try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
                    while (r.readLine() != null) {}
                } catch (IOException ignored) {}
            }).start();
            new Thread(() -> {
                try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getErrorStream()))) {
                    while (r.readLine() != null) {}
                } catch (IOException ignored) {}
            }).start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "exec failed: " + cmd, e);
        }
    }

    public static void applyResolutionAndDensity(Context context, String resolution, String density) {
        try {
            if ("reset".equals(resolution)) {
                Runtime.getRuntime().exec("wm size reset").waitFor();
            } else {
                Runtime.getRuntime().exec("wm size " + resolution).waitFor();
            }
            if ("reset".equals(density)) {
                Runtime.getRuntime().exec("wm density reset").waitFor();
            } else {
                Runtime.getRuntime().exec("wm density " + density).waitFor();
            }
            Log.d(TAG, "Applied resolution " + resolution + " density " + density);
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Failed to apply", e);
        }
    }

    public static String getCurrentSize() {
        try {
            Process p = Runtime.getRuntime().exec("wm size");
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String line; String last = null;
            while ((line = r.readLine()) != null) last = line;
            if (last != null) {
                int idx = last.lastIndexOf(":");
                if (idx != -1) return last.substring(idx + 1).trim();
            }
        } catch (IOException e) {
            Log.e(TAG, "getCurrentSize failed", e);
        }
        return null;
    }

    public static String getCurrentDensity() {
        try {
            Process p = Runtime.getRuntime().exec("wm density");
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String line; String last = null;
            while ((line = r.readLine()) != null) last = line;
            if (last != null) {
                int idx = last.lastIndexOf(":");
                if (idx != -1) return last.substring(idx + 1).trim();
            }
        } catch (IOException e) {
            Log.e(TAG, "getCurrentDensity failed", e);
        }
        return null;
    }
}
