package edu.uvm.cs275.conversationanalysis.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BackgroundUploadReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1;
    public static final String ACTION = "edu.uvm.cs275.conversationanalysis.alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, BackgroundUploadService.class);
        context.startService(i);
    }
}
