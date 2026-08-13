package org.openinit.multicloudfilemanager.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import android.util.Base64

object ConfigPasswordManager {

    private const val PREFS_FILENAME = "rclone_sec_config_prefs"
    private const val KEY_RCLONE_CONFIG_PASS = "rclone_config_password"

    @JvmStatic
    @Synchronized
    fun getConfigPassword(context: Context): String {
        return try {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            var password = sharedPreferences.getString(KEY_RCLONE_CONFIG_PASS, null)
            if (password.isNullOrEmpty()) {
                password = generateSecurePassword()
                sharedPreferences.edit().putString(KEY_RCLONE_CONFIG_PASS, password).apply()
            }
            password
        } catch (e: Exception) {
            FLog.e("ConfigPasswordManager", "Error accessing EncryptedSharedPreferences, fallback to fallback key", e)
            getFallbackPassword(context)
        }
    }

    private fun generateSecurePassword(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE)
    }

    private fun getFallbackPassword(context: Context): String {
        val prefs = context.getSharedPreferences("rclone_sec_fallback", Context.MODE_PRIVATE)
        var pass = prefs.getString(KEY_RCLONE_CONFIG_PASS, null)
        if (pass.isNullOrEmpty()) {
            pass = generateSecurePassword()
            prefs.edit().putString(KEY_RCLONE_CONFIG_PASS, pass).apply()
        }
        return pass
    }
}
