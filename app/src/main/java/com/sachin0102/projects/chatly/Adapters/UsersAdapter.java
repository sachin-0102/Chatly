package com.sachin0102.projects.chatly.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sachin0102.projects.chatly.Activties.ChatActivity;
import com.sachin0102.projects.chatly.Models.User;
import com.sachin0102.projects.chatly.R;
import com.sachin0102.projects.chatly.databinding.RowConversationBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder>{

    Context context;
    ArrayList<User> users;

    public UsersAdapter(Context context, ArrayList<User> users){
        this.context = context;
        this.users = users;
    }
    @NonNull
    @NotNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation,parent,false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UsersViewHolder holder, int position) {
        User user = users.get(position);
        String senderId = FirebaseAuth.getInstance().getUid();

        String senderRoom = senderId + user.getUserId();

        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String lastMessage = snapshot.child("lastMsg").getValue(String.class);
                            long time = snapshot.child("lastMsgTime").getValue(Long.class);

                            holder.binding.lastMessage.setText(lastMessage);
                        }else
                        {
                            holder.binding.lastMessage.setText("Tap to chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
        holder.binding.username.setText(user.getName());

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name",user.getName());
                intent.putExtra("uid", user.getUserId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class UsersViewHolder extends RecyclerView.ViewHolder{

        RowConversationBinding binding;
        public UsersViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}

