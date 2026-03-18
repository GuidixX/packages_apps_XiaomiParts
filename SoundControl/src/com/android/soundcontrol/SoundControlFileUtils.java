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
*
*/

package com.android.soundcontrol;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class SoundControlFileUtils {

    private static final String TAG = "SoundControlFileUtils";

    public static boolean fileWritable(String filename) {
        return fileExists(filename) && new File(filename).canWrite();
    }

    public static boolean fileExists(String filename) {
        if (filename == null) {
            return false;
        }
        return new File(filename).exists();
    }

    public static void setValue(String path, int value) {
        if (fileWritable(path)) {
            if (path == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(new File(path));
                fos.write(Integer.toString(value).getBytes());
                fos.flush();
                fos.close();
                Log.d(TAG, "Set " + path + " = " + value);
            } catch (IOException e) {
                Log.e(TAG, "Failed to write to " + path, e);
            }
        } else {
            Log.w(TAG, "Path not writable: " + path);
        }
    }

    public static void setValue(String path, double value) {
        if (fileWritable(path)) {
            if (path == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(new File(path));
                fos.write(Long.toString(Math.round(value)).getBytes());
                fos.flush();
                fos.close();
                Log.d(TAG, "Set " + path + " = " + value);
            } catch (IOException e) {
                Log.e(TAG, "Failed to write to " + path, e);
            }
        } else {
            Log.w(TAG, "Path not writable: " + path);
        }
    }

    public static void setValue(String path, String value) {
        if (fileWritable(path)) {
            if (path == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(new File(path));
                fos.write(value.getBytes());
                fos.flush();
                fos.close();
                Log.d(TAG, "Set " + path + " = " + value);
            } catch (IOException e) {
                Log.e(TAG, "Failed to write to " + path, e);
            }
        } else {
            Log.w(TAG, "Path not writable: " + path);
        }
    }

    public static int getValue(String path) {
        try {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                byte[] buffer = new byte[256];
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                int length = fis.read(buffer);
                fis.close();
                if (length > 0) {
                    String value = new String(buffer, 0, length).trim();
                    return Integer.parseInt(value);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to read from " + path, e);
        }
        return 0;
    }
}
