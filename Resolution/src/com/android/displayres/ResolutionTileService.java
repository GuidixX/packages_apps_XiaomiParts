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

import android.content.Intent;
import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class ResolutionTileService extends TileService {
    private static final String TAG = "ResolutionTileService";
    private static final String PREF_KEY_INDEX = "resolution_tile_index";

    private int currentIndex = 0;

    @Override
    public void onStartListening() {
        super.onStartListening();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentIndex = prefs.getInt(PREF_KEY_INDEX, 0);
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_ACTIVE);
            updateTileLabel(tile);
        }
    }

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        if (tile == null) return;

        // Click: cycle resolution/density; long-press is handled by QS_TILE_PREFERENCES
        String[] resolutions = getResources().getStringArray(R.array.config_resolutions);
        String[] densities = getResources().getStringArray(R.array.config_densities);
        if (resolutions.length == 0 || densities.length == 0) return;
        if (currentIndex >= resolutions.length) currentIndex = 0;

        currentIndex = (currentIndex + 1) % resolutions.length;
        String res = resolutions[currentIndex];
        String density = densities[currentIndex % densities.length];

        ShellUtils.applyResolutionAndDensity(res, density);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putInt(PREF_KEY_INDEX, currentIndex).apply();
        updateTileLabel(tile);
    }

    private void updateTileLabel(Tile tile) {
        String[] resolutions = getResources().getStringArray(R.array.config_resolutions);
        if (currentIndex >= resolutions.length) currentIndex = 0;
        tile.setLabel(resolutions[currentIndex]);
        tile.updateTile();
    }
}
