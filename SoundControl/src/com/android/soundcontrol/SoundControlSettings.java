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

import android.os.Bundle;
import android.util.Log;
import androidx.preference.Preference;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

public class SoundControlSettings extends SettingsBasePreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "SoundControlSettings";

    private CustomSeekBarPreference mHeadphoneGain;
    private CustomSeekBarPreference mMicrophoneGain;
    private CustomSeekBarPreference mSpeakerGain;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.soundcontrol_settings, rootKey);

        mHeadphoneGain = (CustomSeekBarPreference) findPreference(SoundControlPreferences.PREF_HEADPHONE_GAIN);
        if (mHeadphoneGain != null) {
            mHeadphoneGain.setOnPreferenceChangeListener(this);
        }

        mMicrophoneGain = (CustomSeekBarPreference) findPreference(SoundControlPreferences.PREF_MICROPHONE_GAIN);
        if (mMicrophoneGain != null) {
            mMicrophoneGain.setOnPreferenceChangeListener(this);
        }

        mSpeakerGain = (CustomSeekBarPreference) findPreference(SoundControlPreferences.PREF_SPEAKER_GAIN);
        if (mSpeakerGain != null) {
            mSpeakerGain.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        final int intValue = (int) value;

        Log.d(TAG, "Preference changed: " + key + " = " + intValue);

        switch (key) {
            case SoundControlPreferences.PREF_HEADPHONE_GAIN:
                SoundControlFileUtils.setValue(SoundControlPreferences.HEADPHONE_GAIN_PATH, 
                        intValue + " " + intValue);
                break;

            case SoundControlPreferences.PREF_MICROPHONE_GAIN:
                SoundControlFileUtils.setValue(SoundControlPreferences.MICROPHONE_GAIN_PATH, intValue);
                break;

            case SoundControlPreferences.PREF_SPEAKER_GAIN:
                SoundControlFileUtils.setValue(SoundControlPreferences.SPEAKER_GAIN_PATH, intValue);
                break;

            default:
                return false;
        }
        return true;
    }
}
