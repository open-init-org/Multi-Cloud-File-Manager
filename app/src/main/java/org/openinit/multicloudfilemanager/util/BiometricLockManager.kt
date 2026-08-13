package org.openinit.multicloudfilemanager.util

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import org.openinit.multicloudfilemanager.R

object BiometricLockManager {

    @JvmStatic
    var isAppLocked: Boolean = true

    @JvmStatic
    var isPromptShowing: Boolean = false
        private set

    @JvmStatic
    fun isBiometricUnlockEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val key = context.getString(R.string.pref_key_biometric_lock)
        return prefs.getBoolean(key, false)
    }

    @JvmStatic
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
        } else {
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            val canBio = biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
            if (canBio) return true
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            return keyguardManager?.isDeviceSecure == true
        }
    }

    @JvmStatic
    @JvmOverloads
    fun checkAndPromptLock(
        activity: FragmentActivity,
        onSuccess: Runnable? = null
    ) {
        if (!isBiometricUnlockEnabled(activity) || !isAppLocked) {
            return
        }
        promptBiometric(
            activity,
            onSuccess = {
                onSuccess?.run()
            },
            onError = { _ ->
                if (!activity.isFinishing && !activity.isDestroyed) {
                    activity.finishAffinity()
                }
            }
        )
    }

    private fun showOverlay(activity: FragmentActivity) {
        val rootLayout = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        if (rootLayout.findViewById<View>(R.id.biometric_overlay_root) != null) {
            return
        }
        val overlayView = activity.layoutInflater.inflate(R.layout.overlay_biometric_lock, rootLayout, false)
        rootLayout.addView(overlayView)
    }

    private fun hideOverlay(activity: FragmentActivity) {
        val rootLayout = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val overlayView = rootLayout.findViewById<View>(R.id.biometric_overlay_root) ?: return
        rootLayout.removeView(overlayView)
    }

    @JvmStatic
    @JvmOverloads
    fun promptBiometric(
        activity: FragmentActivity,
        onSuccess: Runnable,
        onError: Consumer<String>? = null
    ) {
        if (isPromptShowing || activity.isFinishing || activity.isDestroyed) {
            return
        }

        isPromptShowing = true
        showOverlay(activity)

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isPromptShowing = false
                isAppLocked = false
                hideOverlay(activity)
                onSuccess.run()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                isPromptShowing = false
                onError?.accept(errString.toString())
            }
        }

        try {
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            val builder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.biometric_prompt_title))
                .setSubtitle(activity.getString(R.string.biometric_prompt_subtitle))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
            } else {
                @Suppress("DEPRECATION")
                builder.setDeviceCredentialAllowed(true)
            }

            biometricPrompt.authenticate(builder.build())
        } catch (e: Exception) {
            isPromptShowing = false
            hideOverlay(activity)
            onError?.accept(e.localizedMessage ?: "Biometric error")
        }
    }

    fun interface Consumer<T> {
        fun accept(value: T)
    }
}


