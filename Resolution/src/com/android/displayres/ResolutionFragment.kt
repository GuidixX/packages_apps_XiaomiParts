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

package com.android.displayres

import android.os.Bundle
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.android.settingslib.widget.MainSwitchPreference

class ResolutionFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.resolution_prefs, rootKey)

        val serviceSwitch = findPreference<MainSwitchPreference>("service_enabled")
        val resPref = findPreference<ListPreference>("resolution_index")

        // Load saved service state
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val serviceEnabled = prefs.getBoolean("service_enabled", false)
        serviceSwitch?.isChecked = serviceEnabled

        // Auto-update density when resolution changes (hidden from UI)
        resPref?.setOnPreferenceChangeListener { _, newValue ->
            val resolutions = getStringArray(R.array.config_resolutions)
            val densities = getStringArray(R.array.config_densities)
            
            if (resolutions.isNotEmpty() && densities.isNotEmpty()) {
                val selectedResolution = newValue as String
                val resolutionIndex = resolutions.indexOf(selectedResolution)
                
                if (resolutionIndex >= 0) {
                    // Density is automatically calculated based on resolution index
                    // No need to update UI since density preference is hidden
                }
            }
            true
        }

        // Service switch listener
        serviceSwitch?.addOnSwitchChangeListener { _, isChecked ->
            prefs.edit().putBoolean("service_enabled", isChecked).apply()
            
            if (isChecked) {
                // Apply current settings when service is enabled
                val selectedResolution = resPref?.value ?: ""
                if (selectedResolution.isNotEmpty()) {
                    val resolutions = getStringArray(R.array.config_resolutions)
                    val densities = getStringArray(R.array.config_densities)
                    val resolutionIndex = resolutions.indexOf(selectedResolution)
                    val density = if (resolutionIndex >= 0) densities[resolutionIndex % densities.size] else densities.first()
                    
                    ShellUtils.applyResolutionAndDensity(selectedResolution, density)
                    Toast.makeText(requireContext(), "Resolution service enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Reset to default when service is disabled
                val defaultRes = prefs.getString(Constants.PREF_KEY_DEFAULT_RES, null)
                val defaultDensity = prefs.getString(Constants.PREF_KEY_DEFAULT_DPI, null)
                if (defaultRes != null && defaultDensity != null) {
                    ShellUtils.applyResolutionAndDensity(defaultRes, defaultDensity)
                } else {
                    ShellUtils.applyResolutionAndDensity("reset", "reset")
                }
                Toast.makeText(requireContext(), "Resolution service disabled", Toast.LENGTH_SHORT).show()
            }
        }

        findPreference<Preference>("apply_now")?.setOnPreferenceClickListener {
            val resolutions = getStringArray(R.array.config_resolutions)
            val densities = getStringArray(R.array.config_densities)
            
            if (resolutions.isEmpty() || densities.isEmpty()) {
                Toast.makeText(requireContext(), "No resolutions configured. Please add device overlay.", Toast.LENGTH_LONG).show()
                return@setOnPreferenceClickListener true
            }
            
            val selectedResolution = resPref?.value ?: resolutions.first()
            val resolutionIndex = resolutions.indexOf(selectedResolution)
            val density = if (resolutionIndex >= 0) densities[resolutionIndex % densities.size] else densities.first()
            
            ShellUtils.applyResolutionAndDensity(selectedResolution, density)
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit().putString(Constants.PREF_KEY_RES_VALUE, selectedResolution)
                .putString(Constants.PREF_KEY_DPI_VALUE, density).apply()
            Toast.makeText(requireContext(), "Resolution applied", Toast.LENGTH_SHORT).show()
            true
        }

        findPreference<Preference>("reset_default")?.setOnPreferenceClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val defRes = prefs.getString(Constants.PREF_KEY_DEFAULT_RES, null)
            val defDensity = prefs.getString(Constants.PREF_KEY_DEFAULT_DPI, null)
            if (defRes != null && defDensity != null) {
                ShellUtils.applyResolutionAndDensity(defRes, defDensity)
                prefs.edit().remove(Constants.PREF_KEY_RES_VALUE)
                    .remove(Constants.PREF_KEY_DPI_VALUE).apply()
            } else {
                ShellUtils.applyResolutionAndDensity("reset", "reset")
                prefs.edit().remove(Constants.PREF_KEY_RES_VALUE)
                    .remove(Constants.PREF_KEY_DPI_VALUE).apply()
            }
            Toast.makeText(requireContext(), "Reset to default", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun getStringArray(id: Int): Array<String> = resources.getStringArray(id)
}
