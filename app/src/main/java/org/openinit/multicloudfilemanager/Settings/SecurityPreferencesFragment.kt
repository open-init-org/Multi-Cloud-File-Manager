package org.openinit.multicloudfilemanager.Settings

import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.openinit.multicloudfilemanager.R
import org.openinit.multicloudfilemanager.util.BiometricLockManager

class SecurityPreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_security_preferences, rootKey)
        requireActivity().title = getString(R.string.preference_category_security)
        setupBiometricLockPreference()
    }

    private fun setupBiometricLockPreference() {
        val biometricLockKey = getString(R.string.pref_key_biometric_lock)
        val biometricPreference = findPreference<SwitchPreferenceCompat>(biometricLockKey)
        biometricPreference?.setOnPreferenceChangeListener { preference, newValue ->
            val enable = newValue as Boolean
            if (enable) {
                val context = context ?: return@setOnPreferenceChangeListener false
                if (!BiometricLockManager.canAuthenticate(context)) {
                    Toast.makeText(
                        context,
                        R.string.biometric_not_available,
                        Toast.LENGTH_LONG
                    ).show()
                    false
                } else {
                    val activity = activity ?: return@setOnPreferenceChangeListener false
                    BiometricLockManager.promptBiometric(
                        activity,
                        onSuccess = {
                            if (isAdded && getContext() != null) {
                                (preference as? SwitchPreferenceCompat)?.isChecked = true
                            }
                        },
                        onError = { errorMsg ->
                            if (isAdded && getContext() != null) {
                                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    false
                }
            } else {
                true
            }
        }
    }
}
