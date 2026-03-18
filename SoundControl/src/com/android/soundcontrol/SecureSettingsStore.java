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

import android.content.ContentResolver;
import android.provider.Settings;
import androidx.preference.PreferenceDataStore;

public class SecureSettingsStore extends PreferenceDataStore {

    private final ContentResolver mContentResolver;

    public SecureSettingsStore(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return getInt(key, defValue ? 1 : 0) != 0;
    }

    @Override
    public float getFloat(String key, float defValue) {
        return Settings.Secure.getFloat(mContentResolver, key, defValue);
    }

    @Override
    public int getInt(String key, int defValue) {
        return Settings.Secure.getInt(mContentResolver, key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return Settings.Secure.getLong(mContentResolver, key, defValue);
    }

    @Override
    public String getString(String key, String defValue) {
        String result = Settings.Secure.getString(mContentResolver, key);
        return result == null ? defValue : result;
    }

    @Override
    public void putBoolean(String key, boolean value) {
        putInt(key, value ? 1 : 0);
    }

    @Override
    public void putFloat(String key, float value) {
        Settings.Secure.putFloat(mContentResolver, key, value);
    }

    @Override
    public void putInt(String key, int value) {
        Settings.Secure.putInt(mContentResolver, key, value);
    }

    @Override
    public void putLong(String key, long value) {
        Settings.Secure.putLong(mContentResolver, key, value);
    }

    @Override
    public void putString(String key, String value) {
        Settings.Secure.putString(mContentResolver, key, value);
    }
}
