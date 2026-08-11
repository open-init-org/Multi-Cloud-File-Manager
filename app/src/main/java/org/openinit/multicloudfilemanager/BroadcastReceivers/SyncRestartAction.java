package org.openinit.multicloudfilemanager.BroadcastReceivers;

import static org.openinit.multicloudfilemanager.workmanager.SyncWorker.EXTRA_TASK_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.openinit.multicloudfilemanager.workmanager.SyncManager;

/**
 * This class requires a receiver declaration in the manifest
 */
public class SyncRestartAction extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SyncManager sm = new SyncManager(context);
        sm.queue(intent.getLongExtra(EXTRA_TASK_ID, -1));
    }
}
