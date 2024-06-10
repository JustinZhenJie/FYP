package com.example.fyp_ilikethatcoffee;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.fyp_ilikethatcoffee.MainActivity;
import com.example.fyp_ilikethatcoffee.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";
    private static final String CHANNEL_ID = "geofence_notification_channel";
    private static final int NOTIFICATION_ID = 123;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Geofence transition received");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null");
            return;
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofence error: " + geofencingEvent.getErrorCode());
            return;
        }

        List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
        if (triggeredGeofences == null) {
            Log.e(TAG, "Triggered geofences list is null");
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        for (Geofence geofence : triggeredGeofences) {
            String storeName = geofence.getRequestId();
            Log.d(TAG, "Triggered geofence: " + storeName);

            switch (geofenceTransition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Log.d(TAG, "Geofence enter event for store: " + storeName);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendNotification(context, storeName, "Entered geofence area");
                    }
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    Log.d(TAG, "Geofence exit event for store: " + storeName);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendNotification(context, storeName, "Exited geofence area");
                    }
                    break;
                default:
                    Log.e(TAG, "Unknown geofence transition: " + geofenceTransition);
                    break;
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void sendNotification(Context context, String storeName, String message) {
        // Create an explicit intent for launching the app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Create the notification message with the store name
        String notificationMessage = "Drop by " + storeName + " Now.";

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.coffeebeanlogo)
                .setContentTitle("Need a quick caffeine fix?")
                .setContentText(notificationMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Get the notification manager and show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
