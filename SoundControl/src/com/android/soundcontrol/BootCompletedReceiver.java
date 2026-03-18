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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "SoundControlBootReceiver";
    private static final boolean DEBUG = false;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (DEBUG) Log.d(TAG, "Boot completed, applying sound control settings");

            // Restore headphone gain
            int headphoneGain = Settings.Secure.getInt(context.getContentResolver(),
                    SoundControlPreferences.PREF_HEADPHONE_GAIN, 
                    SoundControlPreferences.DEFAULT_HEADPHONE_GAIN);
            SoundControlFileUtils.setValue(SoundControlPreferences.HEADPHONE_GAIN_PATH, 
                    headphoneGain + " " + headphoneGain);

            // Restore microphone gain
            int microphoneGain = Settings.Secure.getInt(context.getContentResolver(),
                    SoundControlPreferences.PREF_MICROPHONE_GAIN, 
                    SoundControlPreferences.DEFAULT_MICROPHONE_GAIN);
            SoundControlFileUtils.setValue(SoundControlPreferences.MICROPHONE_GAIN_PATH, microphoneGain);

            // Restore speaker gain
            int speakerGain = Settings.Secure.getInt(context.getContentResolver(),
                    SoundControlPreferences.PREF_SPEAKER_GAIN, 
                    SoundControlPreferences.DEFAULT_SPEAKER_GAIN);
            SoundControlFileUtils.setValue(SoundControlPreferences.SPEAKER_GAIN_PATH, speakerGain);

            if (DEBUG) {
                Log.d(TAG, "Applied headphone_gain=" + headphoneGain);
                Log.d(TAG, "Applied microphone_gain=" + microphoneGain);
                Log.d(TAG, "Applied speaker_gain=" + speakerGain);
            }
        }
    }
}
