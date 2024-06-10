package com.example.fyp_ilikethatcoffee;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.fyp_ilikethatcoffee.GeofenceBroadcastReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.maps.model.LatLng;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeofenceService extends Service {

    private static final String TAG = GeofenceService.class.getSimpleName();
    private static final int GEOFENCE_RADIUS = 1000; // in meters
    private static final String CHANNEL_ID = "geofence_notification_channel";
    private static final int NOTIFICATION_ID = 123;
    private GeofencingClient geofencingClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "GeofenceService onCreate");
        geofencingClient = LocationServices.getGeofencingClient(this);
        createGeofencesFromStoreData();

        // Start the service as a foreground service
        startForegroundService();
    }

    private void startForegroundService() {
        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Geofence Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Create the notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Geofence Service")
                .setContentText("Monitoring geofences")
                .setSmallIcon(R.drawable.coffeebeanlogo)
                .build();

        // Start the service as foreground
        startForeground(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private LatLng geocodeAddress(String storeAddress) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(storeAddress, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                Log.d(TAG, "Latitude for address " + storeAddress + ": " + latitude);
                Log.d(TAG, "Longitude for address " + storeAddress + ": " + longitude);
                return new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error geocoding address: " + e.getMessage());
        }
        return null;
    }

    private static class GeocodeAddressAsyncTask extends AsyncTask<Void, Void, LatLng> {
        private WeakReference<GeofenceService> serviceRef;
        private String storeName;
        private String storeAddress;

        GeocodeAddressAsyncTask(GeofenceService service, String storeName, String storeAddress) {
            serviceRef = new WeakReference<>(service);
            this.storeName = storeName;
            this.storeAddress = storeAddress;
        }

        @Override
        protected LatLng doInBackground(Void... voids) {
            GeofenceService service = serviceRef.get();
            if (service == null) return null;
            Log.d(TAG, "GeocodeAddressAsyncTask doInBackground for store: " + storeName);
            // Perform network operations in the background
            return service.geocodeAddress(storeAddress);
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            GeofenceService service = serviceRef.get();
            if (service == null || latLng == null) return;

            Log.d(TAG, "GeocodeAddressAsyncTask onPostExecute for store: " + storeName);
            Log.d(TAG, "Latitude for store " + storeName + ": " + latLng.latitude);
            Log.d(TAG, "Longitude for store " + storeName + ": " + latLng.longitude);
            // This method is executed on the main thread after doInBackground completes
            // Once you have the latitude and longitude, create a Geofence object
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(storeName)
                    .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            // Create a GeofencingRequest
            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build();

            // Check for location permission again before registering the geofence
            if (ActivityCompat.checkSelfPermission(service, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(service, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permissions granted for store: " + storeName);
                // Register the geofence
                GeofencingClient geofencingClient = LocationServices.getGeofencingClient(service);
                geofencingClient.addGeofences(geofencingRequest, service.getGeofencePendingIntent())
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofence added successfully for store: " + storeName))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to add geofence for store: " + storeName + ", Error: " + e.getMessage()));
            } else {
                // Permission is not granted, handle this case
                Log.e(TAG, "Location permissions not granted for store: " + storeName);
            }
        }
    }


    private void createGeofencesFromStoreData() {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions are not granted, handle this case
            Log.e(TAG, "Location permissions not granted");
            return;
        }

        // Permissions are granted, proceed with adding geofences
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference storesRef = db.collection("Store");

        storesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String storeName = documentSnapshot.getString("StoreName");
                String storeAddress = documentSnapshot.getString("StoreAddress");

                // Create an instance of the AsyncTask and execute it
                new GeocodeAddressAsyncTask(this, storeName, storeAddress).execute();
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching store data: " + e.getMessage()));
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        intent.setAction("com.example.fyp_ilikethatcoffee.ACTION_GEOFENCE_EVENT");
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

}