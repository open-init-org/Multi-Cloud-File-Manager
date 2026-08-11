package org.openinit.multicloudfilemanager.Activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.openinit.multicloudfilemanager.R
import org.openinit.multicloudfilemanager.workmanager.SyncManager
import org.openinit.multicloudfilemanager.workmanager.SyncWorker.Companion.EXTRA_TASK_ID
import org.openinit.multicloudfilemanager.workmanager.SyncWorker.Companion.TASK_SYNC_ACTION

class ShortcutServiceActivity : AppCompatActivity() {

    private val TAG = "ShortcutServiceActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveTaskToBack(true)

        Log.e(TAG, "Recieved start signal!")

        if(intent.action == TASK_SYNC_ACTION) {
            Log.e(TAG, "Recieved valid intent.")
            val id = intent.extras?.getLong(EXTRA_TASK_ID)
            if(id != null) {
                SyncManager(this.baseContext).queue(id)
                Toast.makeText(this, getString(R.string.shortcut_start_service), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.shortcut_missing_id), Toast.LENGTH_SHORT).show()
            }
        }

        Log.e(TAG, "Finish up.")
        finish()
    }
}