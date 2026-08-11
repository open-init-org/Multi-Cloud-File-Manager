package ca.pkay.rcloneexplorer.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import ca.pkay.rcloneexplorer.R

object BiometricLockManager {

    @JvmStatic
    var isAppLocked: Boolean = true

    @JvmStatic
    fun isBiometricUnlockEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val key = context.getString(R.string.pref_key_biometric_lock)
        return prefs.getBoolean(key, false)
    }

    @JvmStatic
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    @JvmStatic
    @JvmOverloads
    fun promptBiometric(
        activity: FragmentActivity,
        onSuccess: Runnable,
        onError: Consumer<String>? = null
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isAppLocked = false
                onSuccess.run()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError?.accept(errString.toString())
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_prompt_title))
            .setSubtitle(activity.getString(R.string.biometric_prompt_subtitle))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun interface Consumer<T> {
        fun accept(value: T)
    }
}
