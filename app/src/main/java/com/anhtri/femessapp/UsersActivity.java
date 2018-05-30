package com.anhtri.femessapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import com.anhtri.model.Users;

public class UsersActivity extends AppCompatActivity {

    private Toolbar users_appbar;
    private RecyclerView users_list;
    private CircleImageView avatar_user_item;

    private DatabaseReference usersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        users_appbar = findViewById(R.id.users_appbar);
        setSupportActionBar(users_appbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        users_list = findViewById(R.id.users_list);
        users_list.setHasFixedSize(true);
        users_list.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setUserImage(model.getThumb_image());

                final String user_id = getRef(position).getKey();

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_singleline_layout, parent, false);

                return new UsersViewHolder(view);
            }
        };

        users_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public class UsersViewHolder extends RecyclerView.ViewHolder{
        View view;
        public UsersViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            TextView txtName_user_item = view.findViewById(R.id.txtName_user_item);
            txtName_user_item.setText(name);
        }

        public void setStatus(String status) {
            TextView txtStatus_user_item = view.findViewById(R.id.txtStatus_user_item);
            txtStatus_user_item.setText(status);
        }

        public void setUserImage(String thumb_image) {
            CircleImageView userImage = view.findViewById(R.id.avatar_user_item);

            Picasso.get().load(thumb_image).into(userImage);

        }
    }


}
