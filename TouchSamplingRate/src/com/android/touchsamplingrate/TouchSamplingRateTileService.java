/*
* Copyright (C) 2025 GuidixX
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
package com.android.touchsamplingrate;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.preference.PreferenceManager;

import com.android.touchsamplingrate.utils.FileUtils;

public class TouchSamplingRateTileService extends TileService {

    private static final String TOUCH_SAMPLING_RATE_ENABLE_KEY = "touch_sampling_rate_enable";
    private static final String TOUCH_SAMPLING_RATE_NODE = "/sys/devices/virtual/touch/touch_dev/bump_sample_rate";

    private void updateUI(boolean enabled) {
        final Tile tile = getQsTile();
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateUI(sharedPrefs.getBoolean(TOUCH_SAMPLING_RATE_ENABLE_KEY, false));
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean enabled = !(sharedPrefs.getBoolean(TOUCH_SAMPLING_RATE_ENABLE_KEY, false));
        FileUtils.writeLine(TOUCH_SAMPLING_RATE_NODE, enabled ? "1" : "0");
        sharedPrefs.edit().putBoolean(TOUCH_SAMPLING_RATE_ENABLE_KEY, enabled).commit();
        updateUI(enabled);
    }
}
