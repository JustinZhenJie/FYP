package com.example.fyp_ilikethatcoffee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.example.fyp_ilikethatcoffee.Dialog.ProfileDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db; // Firestore instance
    private EditText loginEmail, loginPassword;
    private Button loginButton, LoginButtonStore, LoginButtonAdmin;
    private TextView forgetPasswordTextView, redirectSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        loginEmail = findViewById(R.id.LoginEmail);
        loginPassword = findViewById(R.id.LoginPassword);
        loginButton = findViewById(R.id.LoginButton);
        forgetPasswordTextView = findViewById(R.id.ForgetPasswordTextView);
        redirectSignup = findViewById(R.id.redirectSignup);
        /*
        LoginButtonStore = findViewById(R.id.LoginButtonStore);
        LoginButtonAdmin = findViewById(R.id.LoginButtonAdmin);


        LoginButtonStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, StoreOwnerProfileActivity.class));
            }
        });

        LoginButtonAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });
        */


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!password.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        // Check if the user is enabled
                                        db.collection("UserAccount").document(email)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if (documentSnapshot.exists()) {

                                                            checkDatabaseInfo(documentSnapshot);

                                                            Boolean isEnabled = documentSnapshot.getBoolean("IsEnable");
                                                            if (Boolean.TRUE.equals(isEnabled)) {
                                                                // If user is enabled, proceed as before
                                                                // If statement to check user type
                                                                String userType = documentSnapshot.getString("UserType");
                                                                if (userType.equals("Consumer")) {
                                                                    String username = documentSnapshot.getString("UserName");
                                                                    String email = documentSnapshot.getString("Email");
                                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                    intent.putExtra("USERNAME", username);
                                                                    intent.putExtra("Email", email);
                                                                    startActivity(intent);
                                                                    finish(); // Finish LoginActivity
                                                                } else if (userType.equals("StoreOwner")) {
                                                                    String username = documentSnapshot.getString("UserName");
                                                                    Intent intent = new Intent(LoginActivity.this, StoreOwnerProfileActivity.class);
                                                                    intent.putExtra("USERNAME", username);
                                                                    startActivity(intent);
                                                                } else if (userType.equals("Admin")){
                                                                    String username = documentSnapshot.getString("UserName");
                                                                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                                                    intent.putExtra("USERNAME", username);
                                                                    startActivity(intent);
                                                                }
                                                            } else {
                                                                // User is suspended
                                                                Toast.makeText(LoginActivity.this, "User is suspended", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(LoginActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(LoginActivity.this, "Failed to retrieve username", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loginPassword.setError("Password cannot be empty");
                    }
                } else if (email.isEmpty()) {
                    loginEmail.setError("Email cannot be empty");
                } else {
                    loginEmail.setError("Please enter a valid email");
                }
            }
        });

        redirectSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }

    private void checkDatabaseInfo(DocumentSnapshot documentSnapshot) {
        Boolean isEnabled = documentSnapshot.getBoolean("IsEnable");
        String BIO = documentSnapshot.getString("BIO");
        String DOB = documentSnapshot.getString("DOB");
        String Image = documentSnapshot.getString("Image");
        if (Boolean.TRUE.equals(isEnabled)) {

            String userType = documentSnapshot.getString("UserType");
            if (userType.equals("Consumer")) {
                String username = documentSnapshot.getString("UserName");
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish(); // Finish LoginActivity
            } else if (userType.equals("StoreOwner")) {
                String username = documentSnapshot.getString("UserName");
                Intent intent = new Intent(LoginActivity.this, StoreOwnerHomeActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            } else if (userType.equals("Admin")) {
                String username = documentSnapshot.getString("UserName");
                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        } else {
            // User is suspended
            Toast.makeText(LoginActivity.this, "User is suspended", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProfileDialog() {
        ProfileDialog profileDialog = new ProfileDialog(LoginActivity.this);
        profileDialog.showDialog();
    }
}
