package com.sachin0102.projects.chatly.Activties;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sachin0102.projects.chatly.Models.User;
import com.sachin0102.projects.chatly.databinding.ActivitySetupProfileBinding;

import org.jetbrains.annotations.NotNull;

public class SetupProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private FirebaseDatabase database;

    private FirebaseStorage storage;


    Uri selectedImage;


    ProgressDialog dialog;

    ActivitySetupProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        getSupportActionBar().hide();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating profile...");
        dialog.setCancelable(false);

        binding.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager manager  = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
            }
        });

        binding.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.namebox.getText().toString();

                if(name.isEmpty()){
                    binding.namebox.setError("Please type a name");
                    return;
                }
                if (selectedImage != null) {
                    dialog.show();
                    StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        String uid= auth.getUid();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String name = binding.namebox.getText().toString();
                                        User user = new User(uid,name,phone,imageUrl);
                                        database.getReference()
                                                .child("users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        dialog.dismiss();
                                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
                else
                {
                    String uid= auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();
                    User user = new User(uid,name,phone,"No image");
                    database.getReference()
                            .child("users")
                            .child(uid)
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null){
            if(data.getData()!=null)
            {
                binding.profileImageView.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}