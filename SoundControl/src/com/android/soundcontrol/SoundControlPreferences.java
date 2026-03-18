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

public class SoundControlPreferences {

    // Preference keys
    public static final String PREF_HEADPHONE_GAIN = "headphone_gain";
    public static final String PREF_MICROPHONE_GAIN = "microphone_gain";
    public static final String PREF_SPEAKER_GAIN = "speaker_gain";

    // sysfs paths
    public static final String HEADPHONE_GAIN_PATH = "/sys/kernel/sound_control/headphone_gain";
    public static final String MICROPHONE_GAIN_PATH = "/sys/kernel/sound_control/mic_gain";
    public static final String SPEAKER_GAIN_PATH = "/sys/kernel/sound_control/speaker_gain";

    // Default values
    public static final int DEFAULT_HEADPHONE_GAIN = 0;
    public static final int DEFAULT_MICROPHONE_GAIN = 0;
    public static final int DEFAULT_SPEAKER_GAIN = 0;

    // Min/Max values
    public static final int HEADPHONE_GAIN_MIN = -40;
    public static final int HEADPHONE_GAIN_MAX = 20;
    public static final int MICROPHONE_GAIN_MIN = -10;
    public static final int MICROPHONE_GAIN_MAX = 20;
    public static final int SPEAKER_GAIN_MIN = -10;
    public static final int SPEAKER_GAIN_MAX = 20;
}
