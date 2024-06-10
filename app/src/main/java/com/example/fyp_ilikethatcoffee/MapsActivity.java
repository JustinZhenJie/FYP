package com.example.fyp_ilikethatcoffee;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.widget.SearchView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private HashMap<Marker, Store> markerStoreMap = new HashMap<>();
    private boolean mapMovedByUser = false;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private SearchView searchView;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                handleSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        if (getIntent().hasExtra("USERNAME")) {
            username = getIntent().getStringExtra("USERNAME");
        }

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create location request
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000) // 10 seconds
                .setFastestInterval(5000); // 5 seconds

        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    updateMapWithUserLocation(location);
                }
            }
        };

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Disable zoom gestures
        mMap.getUiSettings().setZoomGesturesEnabled(false);

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Set up marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            // Show bottom sheet with store details
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
            CircleImageView storeImageView = bottomSheetView.findViewById(R.id.store_image);
            TextView storeTitleTextView = bottomSheetView.findViewById(R.id.store_title);
            TextView storeAddressTextView = bottomSheetView.findViewById(R.id.store_address);
            View cardView = bottomSheetView.findViewById(R.id.cardViewStore);

            // Get the Store object associated with the marker
            Store store = markerStoreMap.get(marker);
            if (store != null) {
                // Set the store image using Picasso or Glide (assuming ImageUrl is the field name)
                Picasso.get().load(store.getImageUrl()).placeholder(R.drawable.store_profile_default).into(storeImageView);

                // Set store name and address
                storeTitleTextView.setText(store.getStoreName());
                storeAddressTextView.setText(store.getStoreAddress());

                // Set up the CardView click listener
                cardView.setOnClickListener(v -> {
                    Intent intent = new Intent(MapsActivity.this, ConsumerStoreProfileActivity.class);
                    intent.putExtra("StoreName", store.getStoreName());
                    intent.putExtra("StoreDesc", store.getStoreDesc());
                    intent.putExtra("StoreAddress", store.getStoreAddress());
                    intent.putExtra("StoreEmail", store.getUserAccountId());
                    intent.putExtra("ImageUrl", store.getImageUrl());
                    intent.putExtra("USERNAME", "USERNAME");
                    startActivity(intent);
                });

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MapsActivity.this);
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            } else {
                Log.e(TAG, "Store object is null for marker: " + marker.getTitle());
            }
            return true;
        });

        // Set up on camera move started listener to detect map movement by the user
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                mapMovedByUser = true;
            }
        });

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Enable My Location button and display current location
        mMap.setMyLocationEnabled(true);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null);
    }

    private void updateMapWithUserLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (!mapMovedByUser) {
            // Move camera only if the map hasn't been moved by the user
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }

        // Fetch nearby coffee shops
        fetchNearbyStores(location.getLatitude(), location.getLongitude());
    }

    private void fetchNearbyStores(double userLatitude, double userLongitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference storesRef = db.collection("Store");

        storesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Store store = documentSnapshot.toObject(Store.class);
                LatLng latLng = convertAddressToLatLng(store.getStoreAddress());
                if (latLng != null) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(store.getStoreName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    // Associate the marker with the Store object
                    markerStoreMap.put(marker, store);
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching store data: " + e.getMessage()));
    }

    private LatLng convertAddressToLatLng(String address) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        LatLng latLng = null;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLng;
    }

    private void handleSearchQuery(String query) {
        LatLng latLng = convertAddressToLatLng(query);
        if (latLng != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        } else {
            Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
        }
    }
}
