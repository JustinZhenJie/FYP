package com.example.fyp_ilikethatcoffee.Dialog;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.StoreOwnerUpdateProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileDialog {
    Context context;
    Dialog dialog;
    CircleImageView image;
    Button dateOfBirthTv;
    EditText inputBio;
    Button btnUpdate;
    String date;
    Uri uri;
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private FirebaseFirestore db;
    StorageReference mImageRef;


    public ProfileDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        mImageRef = FirebaseStorage.getInstance().getReference().child("Image");
        progressDialog=new ProgressDialog(context);
    }

    private void initDialog() {
        dialog.setContentView(R.layout.layout_profile_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void setImageUri(Uri imageuri) {
        uri = imageuri;
        image.setImageURI(uri);
    }

    public void showDialog() {
        initDialog();
        dateOfBirthTv = dialog.findViewById(R.id.dateOfBirth);
        inputBio = dialog.findViewById(R.id.inputBio);
        btnUpdate = dialog.findViewById(R.id.btnUpdate);
        image = dialog.findViewById(R.id.image);
        dateOfBirthTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String bio=inputBio.getText().toString();
                if (bio == null || bio.isEmpty()) {
                    Toast.makeText(context, "Please your bio", Toast.LENGTH_SHORT).show();
                } else if (uri == null) {
                    Toast.makeText(context, "Please select profile image", Toast.LENGTH_SHORT).show();
                } else if (date == null) {
                    Toast.makeText(context, "Please select DOB", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.setMessage("Updating...");
                    progressDialog.show();
                    updateProfile(bio, date, uri);

                }
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.create();
        dialog.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        ((Activity) context).startActivityForResult(intent, 1111);
    }


    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Handle date selection
                        date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        dateOfBirthTv.setText(date);
                    }
                },
                year, month, dayOfMonth
        );
        datePickerDialog.show();
    }


    private void updateProfile(String bio, String dob, Uri image_Url) {

        DocumentReference userRef = db.collection("UserAccount").document(mUser.getEmail());

        Map<String, Object> updates = new HashMap<>();
        updates.put("DOB", dob);
        updates.put("BIO", bio);

        mImageRef.child(mUser.getUid()).putFile(image_Url).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mImageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            updates.put("Image", uri.toString());
                            userRef.update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        dialog.dismiss();

                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "" + e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(context, "" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
