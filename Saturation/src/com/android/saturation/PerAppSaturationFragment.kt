/*
 * Copyright (C) 2025 GuidixX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.saturation

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PerAppSaturationFragment : Fragment() {
    private lateinit var recycler: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.per_app_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.per_app_recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = Adapter(loadApps())
    }

    private fun loadApps(): List<AppItem> {
        val pm = requireContext().packageManager
        val apps = mutableListOf<AppItem>()
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (ai in packages) {
            if (pm.getLaunchIntentForPackage(ai.packageName) == null) continue
            val label = ai.loadLabel(pm)?.toString() ?: ai.packageName
            val icon = ai.loadIcon(pm)
            apps.add(AppItem(ai.packageName, label, icon))
        }
        apps.sortBy { it.label.lowercase() }
        return apps
    }

    private inner class Adapter(private val data: List<AppItem>) : RecyclerView.Adapter<VH>() {
        private val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        private val disabled = prefs.getStringSet(Constants.KEY_PER_APP_DISABLED_PACKAGES, emptySet())!!.toMutableSet()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = layoutInflater.inflate(R.layout.per_app_list_item, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = data[position]
            holder.title.text = item.label
            holder.summary.text = item.packageName
            holder.icon.setImageDrawable(item.icon)
            holder.switch.isChecked = !disabled.contains(item.packageName)
            holder.switch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                if (isChecked) disabled.remove(item.packageName) else disabled.add(item.packageName)
                prefs.edit().putStringSet(Constants.KEY_PER_APP_DISABLED_PACKAGES, disabled).apply()
            }
        }

        override fun getItemCount(): Int = data.size
    }

    private class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val title: TextView = view.findViewById(R.id.app_title)
        val summary: TextView = view.findViewById(R.id.app_pkg)
        val switch: android.widget.Switch = view.findViewById(R.id.app_switch)
    }
}

data class AppItem(val packageName: String, val label: String, val icon: android.graphics.drawable.Drawable)
