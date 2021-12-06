package io.github.mzdluo123.mirai.android.ui.setting

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import io.github.mzdluo123.mirai.android.R


class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "setting"
        setPreferencesFromResource(R.xml.setting_screen, rootKey)

        findPreference<EditTextPreference>("log_buffer_preference")?.apply {
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }


        findPreference<EditTextPreference>("status_refresh_count")?.apply {
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }

        findPreference<SwitchPreference>("ignore_battery_optimization")?.apply {
            /*
            setOnPreferenceClickListener { preference ->
                true
            }
            */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:" + requireActivity().packageName)
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasIgnored = ContextCompat.getSystemService(
                requireContext(),
                PowerManager::class.java
            )!!.isIgnoringBatteryOptimizations(requireContext().packageName)

            PreferenceManager.getDefaultSharedPreferences(requireContext()).apply {
                edit().putBoolean("ignore_battery_optimization", hasIgnored).apply()
            }
        }

    }

}