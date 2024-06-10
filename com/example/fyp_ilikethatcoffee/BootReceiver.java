package com.example.fyp_ilikethatcoffee;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if the device has finished booting
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Start GeofenceService
            Intent serviceIntent = new Intent(context, GeofenceService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}

