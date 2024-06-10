package com.example.fyp_ilikethatcoffee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 201;
    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    private EditText signup_Email, signup_Username, signup_Password, signup_Repassword;
    private TextView redirectLogin;
    private Button signup_Button, signup_Button_Store_owner;

    public SignUpActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        signup_Email = findViewById(R.id.signup_Email);
        signup_Username = findViewById(R.id.signup_Username);
        signup_Password = findViewById(R.id.signup_Password);
        signup_Repassword = findViewById(R.id.signup_Repassword);
        signup_Button = findViewById(R.id.signup_Button);
        signup_Button_Store_owner = findViewById(R.id.signup_Button_Store_owner);
        redirectLogin = findViewById(R.id.redirectLogin);

        signup_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signup_Email.getText().toString().trim().toLowerCase();
                String username = signup_Username.getText().toString().trim();
                String password = signup_Password.getText().toString().trim();
                String repassword = signup_Repassword.getText().toString().trim();

                if (email.isEmpty()){
                    signup_Email.setError("Email cannot be empty");
                    return;
                }
                if (username.isEmpty()){
                    signup_Username.setError("Username cannot be empty");
                    return;
                }
                if (password.isEmpty()){
                    signup_Password.setError("Password cannot be empty");
                    return;
                }
                if (!password.equals(repassword)) {
                    signup_Repassword.setError("Passwords do not match");
                    return;
                }

                // Create user in Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User creation successful, now store additional data in Firestore
                            Map<String, Object> userAccount = new HashMap<>();
                            userAccount.put("Email", email);
                            userAccount.put("UserName", username);
                            userAccount.put("Password", password);
                            userAccount.put("IsEnable", true); // Add IsEnable field
                            userAccount.put("UserType", "Consumer"); // Add UserType field

                            // Add user data to Firestore
                            db.collection("UserAccount").document(email)
                                    .set(userAccount)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(SignUpActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("SignUpActivity", "Error adding user data to Firestore: " + e.getMessage());
                                            Toast.makeText(SignUpActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        signup_Button_Store_owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signup_Email.getText().toString().trim().toLowerCase();
                String username = signup_Username.getText().toString().trim();
                String password = signup_Password.getText().toString().trim();
                String repassword = signup_Repassword.getText().toString().trim();

                if (email.isEmpty()){
                    signup_Email.setError("Email cannot be empty");
                    return;
                }
                if (username.isEmpty()){
                    signup_Username.setError("Username cannot be empty");
                    return;
                }
                if (password.isEmpty()){
                    signup_Password.setError("Password cannot be empty");
                    return;
                }
                if (!password.equals(repassword)) {
                    signup_Repassword.setError("Passwords do not match");
                    return;
                }

                // Create user in Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User creation successful, now store additional data in Firestore
                            Map<String, Object> userAccount = new HashMap<>();
                            userAccount.put("Email", email);
                            userAccount.put("UserName", username);
                            userAccount.put("Password", password);
                            userAccount.put("IsEnable", true); // Add IsEnable field
                            userAccount.put("UserType", "StoreOwner"); // Add UserType field

                            // Add user data to Firestore
                            db.collection("UserAccount").document(email)
                                    .set(userAccount)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(SignUpActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignUpActivity.this, StoreOwnerFirstTineLogIn.class);
                                            intent.putExtra("USERNAME", username);
                                            startActivity(intent);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("SignUpActivity", "Error adding user data to Firestore: " + e.getMessage());
                                            Toast.makeText(SignUpActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


        redirectLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });




        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Configure sign-in to request the user's ID, email address, and basic
                // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(SignUpActivity.this, gso);

                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);

            // Signed in successfully, redirect user to MainActivity.
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish the current activity to prevent the user from going back to the sign-in screen

            // Optionally, you can also retrieve user information from the GoogleSignInAccount
            String userEmail = account.getEmail();
            Log.e("SignUpActivity", "handleSignInResult: Signed in successfully with email: " + userEmail);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e("SignUpActivity", "signInResult:failed code=" + e.getMessage());
        }
    }


}


