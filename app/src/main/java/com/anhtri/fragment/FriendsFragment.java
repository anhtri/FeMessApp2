package com.anhtri.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anhtri.femessapp.ChatActivity;
import com.anhtri.femessapp.ProfileActivity;
import com.anhtri.femessapp.R;
import com.anhtri.model.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friends_list;

    private DatabaseReference friendDatabase;
    private DatabaseReference usersDatabase;

    private FirebaseAuth auth;


    private String current_user_id;
    private View mainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_friends,container,false);

        auth = FirebaseAuth.getInstance();

        current_user_id = auth.getCurrentUser().getUid();

        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user_id);
        friendDatabase.keepSynced(true);
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDatabase.keepSynced(true);

        friends_list = mainView.findViewById(R.id.friends_list);
        friends_list.setHasFixedSize(true);
        friends_list.setLayoutManager(new LinearLayoutManager(getContext()));


        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friends").child(current_user_id);

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                holder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();

                usersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userImage = dataSnapshot.child("image").getValue().toString();


                        if(dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);
                        }
                        holder.setName(userName);
                        holder.setUserImage(userImage);

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open profile", "Send message"};

                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which==0){
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);
                                        }

                                        if (which==1){
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);

                                        }
                                    }
                                });

                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_singleline_layout, parent, false);
                return new FriendsViewHolder(view);
            }
        };
        friends_list.setAdapter(friendsViewHolderFirebaseRecyclerAdapter);
        friendsViewHolderFirebaseRecyclerAdapter.startListening();

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View view;
        public FriendsViewHolder(View itemView) {
            super(itemView);

            view = itemView;

        }

        public void setDate(String date){

            TextView userStatusView = view.findViewById(R.id.txtStatus_user_item);
            userStatusView.setText(date);

        }

        public void setName(String name){

            TextView userNameView = view.findViewById(R.id.txtName_user_item);
            userNameView.setText(name);

        }

        public void setUserImage(String image) {

            CircleImageView userImage = view.findViewById(R.id.avatar_user_item);
            Picasso.get().load(image).into(userImage);
        }

        public void setUserOnline(String online_status){

            CircleImageView imgOnline = view.findViewById(R.id.imgOnline);
            if (online_status.equals("true"))
                imgOnline.setVisibility(View.VISIBLE);
            else
                imgOnline.setVisibility(View.INVISIBLE);
        }
    }
}
