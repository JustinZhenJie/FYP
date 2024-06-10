package com.example.fyp_ilikethatcoffee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SettingActivity extends AppCompatActivity {

    TextView btnDelete;

    FirebaseAuth mAth;
    FirebaseUser mUser;
    StorageReference mImageRef;

    String documentId;
    String email;

    ProgressDialog progressDialog;
    TextView btnChangePassword;
    TextView btnHelp, btnLogout, contactusTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        btnDelete = findViewById(R.id.deleteAccount);

        contactusTv = findViewById(R.id.contactusTv);
        btnLogout = findViewById(R.id.logoutTv);
        btnHelp = findViewById(R.id.helpTv);
        btnChangePassword = findViewById(R.id.changePasswordTv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting");

        documentId = getIntent().getStringExtra("id");

        mAth = FirebaseAuth.getInstance();
        mUser = mAth.getCurrentUser();
        email = mUser.getEmail();
        mImageRef = FirebaseStorage.getInstance().getReference().child("Image");


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, HelpActivity.class));
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        contactusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("https://fyp-mobile-db.as.r.appspot.com/contact-us.html"));
                startActivity(myWebLink);

            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        dialogBuilder.setView(dialogView);

        EditText editTextNewPassword = dialogView.findViewById(R.id.editTextNewPassword);
        EditText oldPasswordEt = dialogView.findViewById(R.id.oldPasswordEt);
        Button buttonChangePassword = dialogView.findViewById(R.id.buttonChangePassword);

        AlertDialog alertDialog = dialogBuilder.create();


        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = editTextNewPassword.getText().toString();
                String oldPassword = oldPasswordEt.getText().toString();
                if (!TextUtils.isEmpty(newPassword)) {
                    changePassword(oldPassword, newPassword, alertDialog);
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter a new password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.show();
    }

    private void changePassword(String oldPassword, String newPassword, AlertDialog alertDialog) {
        FirebaseUser user = mAth.getCurrentUser();

        if (user != null) {
            // Create a credential using the user's email and current password
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            // Re-authenticate the user
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // User successfully re-authenticated, now update the password
                    user.updatePassword(newPassword).addOnCompleteListener(passwordUpdateTask -> {
                        if (passwordUpdateTask.isSuccessful()) {
                            // Password updated successfully
                            alertDialog.dismiss();
                            Toast.makeText(SettingActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            // Password update failed
                            Toast.makeText(SettingActivity.this, "Failed to update password: " + passwordUpdateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            passwordUpdateTask.getException().printStackTrace();
                        }
                    });
                } else {
                    // Re-authentication failed
                    Toast.makeText(SettingActivity.this, "Re-authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    task.getException().printStackTrace();
                }
            });
        } else {
            // User is not signed in
            Toast.makeText(SettingActivity.this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }


    private void logout() {
        // Stop the GeofenceService
        Intent serviceIntent = new Intent(this, GeofenceService.class);
        stopService(serviceIntent);

        // Perform logout
        mAth.signOut();
        Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are yo sure you want to delete account ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        progressDialog.show();
                        email = mUser.getEmail();
                        mUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                deleteUserAccount(email);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SettingActivity.this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).

                setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create();
        builder.show();

    }


    public void deleteUserAccount(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userAccountsRef = db.collection("UserAccount");

        // Query to filter documents where the "Email" field matches the given email
        Query query = userAccountsRef.whereEqualTo("Email", email);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();

                        // Delete the document with the retrieved document ID
                        db.collection("UserAccount").document(documentId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Document successfully deleted
                                openLoginActivity();
                                progressDialog.dismiss();
                                Toast.makeText(SettingActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                // Handle any errors that may occur while deleting the document
                                Toast.makeText(SettingActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(SettingActivity.this, "No Data Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void openLoginActivity() {
        Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}