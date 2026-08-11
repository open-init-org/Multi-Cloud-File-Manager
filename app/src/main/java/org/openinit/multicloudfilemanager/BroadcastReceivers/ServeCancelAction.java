package org.openinit.multicloudfilemanager.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.openinit.multicloudfilemanager.Services.StreamingService;

public class ServeCancelAction extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serveIntent = new Intent(context, StreamingService.class);
        context.stopService(serveIntent);
    }
}
