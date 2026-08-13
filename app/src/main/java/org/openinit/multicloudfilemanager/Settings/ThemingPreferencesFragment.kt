package org.openinit.multicloudfilemanager.Settings

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.openinit.multicloudfilemanager.R

class ThemingPreferencesFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_theming_preferences, rootKey)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        requireActivity().title = getString(R.string.look_and_feel)

        val new = getString(R.string.pref_key_theme)
        val old = getString(R.string.pref_key_theme_old)

        if(sharedPreferences.contains(old)) {
            sharedPreferences.edit()
                .putString(new, sharedPreferences.getString(new, "0"))
                .remove(old)
                .apply()
        }

        val thumbnailKey = getString(R.string.pref_key_thumbnail_size_limit)
        val thumbnailSizePreference = findPreference<org.openinit.multicloudfilemanager.extract.settings.preferences.FilesizePreference>(thumbnailKey)
        thumbnailSizePreference?.summaryProvider =
            androidx.preference.Preference.SummaryProvider<org.openinit.multicloudfilemanager.extract.settings.preferences.FilesizePreference> { preference ->
                val size = preference.getValue()
                val sizeMb = (size / 1024 / 1024)
                resources.getString(R.string.pref_thumbnails_size_summary, sizeMb.toFloat())
            }

        val languagePreference = findPreference<androidx.preference.Preference>("languagePickerTempKey")
        languagePreference?.summary = org.openinit.multicloudfilemanager.extract.settings.language.LanguagePicker(requireContext()).getCurrentLocale()?.displayLanguage
        languagePreference?.setOnPreferenceClickListener {
            org.openinit.multicloudfilemanager.extract.settings.language.LanguagePicker(requireContext()).showPicker()
            true
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == activity?.getString(R.string.pref_key_theme)) {
            requireActivity().recreate()
        }
    }

}