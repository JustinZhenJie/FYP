package com.example.fyp_ilikethatcoffee;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.fyp_ilikethatcoffee.Dialog.ProfileDialog;
import com.example.fyp_ilikethatcoffee.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private String username;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private ProfileDialog profileDialog;

    // Define constants for permissions requests
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        profileDialog = new ProfileDialog(this);

        checkProfile();
        checkNotificationPermission();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_discover, R.id.navigation_stores)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        if (getIntent().hasExtra("USERNAME")) {
            username = getIntent().getStringExtra("USERNAME");
        }

        // Set navigation item selected listener for the bottom navigation view
        navView.setOnNavigationItemSelectedListener((MenuItem item) -> {
            if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(MainActivity.this, ConsumerProfileActivity.class));
                return true;
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("USERNAME", username);
                navController.navigate(item.getItemId(), bundle);
                return true;
            }
        });

        // Register the BootReceiver
        registerBootReceiver();
    }

    private void checkProfile() {
        db.collection("UserAccount").document(mUser.getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            checkDatabaseInfo(documentSnapshot);
                        } else {
                            Toast.makeText(MainActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to retrieve username", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkDatabaseInfo(DocumentSnapshot documentSnapshot) {
        Boolean isEnabled = documentSnapshot.getBoolean("IsEnable");
        String BIO = documentSnapshot.getString("BIO");
        String DOB = documentSnapshot.getString("DOB");
        String Image = documentSnapshot.getString("Image");

        if (BIO != null && !BIO.isEmpty() && DOB != null && !DOB.isEmpty() && Image != null && !Image.isEmpty()) {
            // Perform any additional actions if required
        } else {
            profileDialog.showDialog();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111 && data != null) {
            profileDialog.setImageUri(data.getData());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        int currentDestinationId = navController.getCurrentDestination().getId();

        MenuItem chatMenuItem = menu.findItem(R.id.action_chat);
        MenuItem notificationsMenuItem = menu.findItem(R.id.navigation_notifications);

        if (currentDestinationId == R.id.navigation_chat) {
            chatMenuItem.setVisible(false);
            notificationsMenuItem.setVisible(false);
        } else if (currentDestinationId == R.id.navigation_chat_list) {
            chatMenuItem.setVisible(false);
        } else if (currentDestinationId == R.id.navigation_add_chat) {
            chatMenuItem.setVisible(false);
            notificationsMenuItem.setVisible(false);
        } else {
            chatMenuItem.setVisible(true);
            notificationsMenuItem.setVisible(true);
        }

        MenuItem addMenuItem = menu.findItem(R.id.action_add);
        addMenuItem.setVisible(currentDestinationId == R.id.navigation_chat_list);

        return true;
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_notifications) {
            navController.navigate(R.id.navigation_notifications);
            return true;
        } else if (id == R.id.action_chat) {
            navController.navigate(R.id.navigation_chat_list);
            return true;
        } else if (id == R.id.action_add){
            navController.navigate(R.id.navigation_add_chat);
        }
        return super.onOptionsItemSelected(item);
    }

    public void hideBottomNavBar() {
        binding.navView.setVisibility(View.GONE);
    }

    public void showBottomNavBar() {
        binding.navView.setVisibility(View.VISIBLE);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE);
            } else {
                checkLocationPermission();
            }
        } else {
            checkLocationPermission(); // For versions below TIRAMISU, directly check location permission.
        }
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showBackgroundPermissionRationale();
            } else {
                startGeofenceService();
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                startGeofenceService();
            }
        }
    }

    private void showBackgroundPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Background Location Permission")
                .setMessage("This app needs background location access to detect geofences even when the app is not in use.")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                    }
                })
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Background location access denied", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void startGeofenceService() {
        Intent serviceIntent = new Intent(this, GeofenceService.class);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else {
            startService(serviceIntent);  // Ensure permissions are checked again if not granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();  // Check again for background location permission
            } else {
                Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGeofenceService();
            } else {
                Toast.makeText(this, "Background location access denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.fyp_ilikethatcoffee.ACTION_GEOFENCE_EVENT");
        registerReceiver(geofenceBroadcastReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(geofenceBroadcastReceiver);
    }


    private final BroadcastReceiver geofenceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d(TAG, "Broadcast received with message: " + message);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    // Method to register the BootReceiver
    private void registerBootReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        BootReceiver bootReceiver = new BootReceiver();
        registerReceiver(bootReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Method to unregister the BootReceiver
    private void unregisterBootReceiver() {
        BootReceiver bootReceiver = new BootReceiver();
        unregisterReceiver(bootReceiver);
    }


}
