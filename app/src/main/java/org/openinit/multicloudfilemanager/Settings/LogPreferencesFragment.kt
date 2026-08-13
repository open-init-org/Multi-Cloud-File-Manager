package org.openinit.multicloudfilemanager.Settings

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.openinit.multicloudfilemanager.R
import org.openinit.multicloudfilemanager.util.FLog
import org.openinit.multicloudfilemanager.extract.extensions.tag
import org.openinit.multicloudfilemanager.extract.settings.preferences.ButtonPreference
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.regex.Pattern


class LogPreferencesFragment : PreferenceFragmentCompat() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_logging_preferences, rootKey)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        requireActivity().title = getString(R.string.advanced_settings_header)

        val shortcutsPreference = findPreference<Preference>("AppShortcutTempKey")
        shortcutsPreference?.setOnPreferenceClickListener {
            showAppShortcutDialog()
            true
        }

        val sigkill = findPreference<Preference>("TempKeySigquit") as ButtonPreference
        sigkill.setButtonText(getString(R.string.pref_send_sigquit_button))
        sigkill.setButtonOnClick {
            sigquitAll()
        }
    }

    private fun showAppShortcutDialog() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return
        }
        val remotes = org.openinit.multicloudfilemanager.Rclone(requireContext()).getRemotes()
        val names = remotes.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(names.size)
        val selectedRemotes = ArrayList<String>()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.app_shortcuts_settings_dialog_title)
            .setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedRemotes.add(names[which])
                } else {
                    selectedRemotes.remove(names[which])
                }
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                val list = ArrayList<org.openinit.multicloudfilemanager.Items.RemoteItem>()
                for (name in selectedRemotes) {
                    for (remote in remotes) {
                        if (remote.name == name) {
                            list.add(remote)
                        }
                    }
                }
                org.openinit.multicloudfilemanager.AppShortcutsHelper.populateAppShortcuts(requireContext(), list)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }


    private fun sigquitAll() {
        Toast.makeText(context, "Multi Cloud File Manager: Stopping everything", Toast.LENGTH_LONG).show()
        try {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec("ps")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            val output = StringBuilder()
            while ((reader.readLine().also { line = it }) != null) {
                output.append('\n')
                output.append(line)
            }

            process.waitFor()

            val regex = "\\s+(\\d+)\\s+\\d+\\s+\\d+\\s+.+librclone.+$"
            val pattern = Pattern.compile(regex, Pattern.MULTILINE)
            val matcher = pattern.matcher(output.toString())

            while (matcher.find()) {
                for (i in 1..matcher.groupCount()) {
                    val pidMatch = matcher.group(i) ?: continue
                    val pid = pidMatch.toInt()
                    FLog.i(tag(), "SIGQUIT to process pid=%s", pid)
                    Process.sendSignal(pid, Process.SIGNAL_QUIT)
                }
            }
            Process.killProcess(Process.myPid())
        } catch (e: IOException) {
            FLog.e(tag(), "Error executing shell commands", e)
        } catch (e: InterruptedException) {
            FLog.e(tag(), "Error executing shell commands", e)
        }
    }
}