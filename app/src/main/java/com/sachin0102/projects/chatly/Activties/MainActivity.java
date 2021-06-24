package com.sachin0102.projects.chatly.Activties;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sachin0102.projects.chatly.Adapters.TopStatusAdapter;
import com.sachin0102.projects.chatly.Adapters.UsersAdapter;
import com.sachin0102.projects.chatly.Models.Status;
import com.sachin0102.projects.chatly.Models.User;
import com.sachin0102.projects.chatly.Models.UserStatus;
import com.sachin0102.projects.chatly.R;
import com.sachin0102.projects.chatly.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    TopStatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    ProgressDialog dialog;

    User user;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading status");
        dialog.setCancelable(false);

        database= FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, users);
        binding.recyclerView.setAdapter(usersAdapter);
        userStatuses = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getUid();;

        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        user= snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


        statusAdapter = new TopStatusAdapter(this,userStatuses);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusList.setLayoutManager(layoutManager);
        binding.statusList.setAdapter(statusAdapter);


        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    users.add(user);
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for(DataSnapshot storySnapshot : snapshot.getChildren()){
                        UserStatus status= new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                        ArrayList<Status> statuses= new ArrayList<>();

                        for(DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()){
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }

                        status.setStatuses(statuses);

                        userStatuses.add(status);

                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.status:
                        Intent intent= new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 75);
                }

                return false;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            if(data.getData()!=null)
            {
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime()+"");

                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getProfileImage());
                                    userStatus.setLastUpdated(date.getTime());

                                    HashMap<String,Object> obj = new HashMap<>();
                                    obj.put("name",userStatus.getName());
                                    obj.put("profileImage",userStatus.getProfileImage());
                                    obj.put("lastUpdated",userStatus.getLastUpdated());

                                    String imageUrl = uri.toString();
                                    Status status= new Status(imageUrl, userStatus.getLastUpdated());

                                    database.getReference()
                                            .child("stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .updateChildren(obj);

                                    database.getReference().child("stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .child("statuses")
                                            .push()
                                            .setValue(status);
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.search:
                Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "setting selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.groups:
                startActivity(new Intent(MainActivity.this,GroupChatActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}