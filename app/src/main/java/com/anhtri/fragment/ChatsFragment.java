package com.anhtri.fragment;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anhtri.femessapp.ChatActivity;
import com.anhtri.femessapp.R;
import com.anhtri.model.Conversations;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
public class ChatsFragment extends Fragment {


    private RecyclerView conversationsList;
    private DatabaseReference conversationReference;
    private DatabaseReference messageReference;
    private DatabaseReference userReference;

    private FirebaseAuth auth;

    private String current_user_id;

    private View view;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chats, container, false);

        conversationsList = view.findViewById(R.id.conversationsList);

        /* Get current user UID */
        auth = FirebaseAuth.getInstance();
        current_user_id = auth.getCurrentUser().getUid();

        /* Reference to Chat/current_user_id and sync */
        conversationReference = FirebaseDatabase.getInstance().getReference().child("Chat").child(current_user_id);
        conversationReference.keepSynced(true);

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        userReference.keepSynced(true);

        messageReference = FirebaseDatabase.getInstance().getReference().child("messages").child(current_user_id);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        conversationsList.setHasFixedSize(true);
        conversationsList.setLayoutManager(linearLayoutManager);



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        Query query = conversationReference.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conversations> options =
                new FirebaseRecyclerOptions.Builder<Conversations>()
                        .setQuery(query, Conversations.class)
                        .build();

        FirebaseRecyclerAdapter <Conversations , ConversationsViewHolder> firebaseConversationAdapter = new FirebaseRecyclerAdapter<Conversations, ConversationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ConversationsViewHolder holder, int position, @NonNull final Conversations model) {

                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = messageReference.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(data, model.isSeen());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                userReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userImage = dataSnapshot.child("image").getValue().toString();

                        if (dataSnapshot.hasChild("online")){

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);

                        }

                        holder.setName(userName);
                        holder.setUserImage(userImage);

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id",list_user_id);
                                chatIntent.putExtra("user_name",userName);
                                startActivity(chatIntent);
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
            public ConversationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_singleline_layout, parent, false);
                return new ChatsFragment.ConversationsViewHolder(view);
            }

        };

        conversationsList.setAdapter(firebaseConversationAdapter);
        firebaseConversationAdapter.startListening();
    }

    private class ConversationsViewHolder extends RecyclerView.ViewHolder {
        View view;
        public ConversationsViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setMessage(String message, boolean isSeen){

            TextView txtStatus_user_item = view.findViewById(R.id.txtStatus_user_item);
            txtStatus_user_item.setText(message);

            if (isSeen){
                txtStatus_user_item.setTypeface(txtStatus_user_item.getTypeface(), Typeface.BOLD);
            }else {
                txtStatus_user_item.setTypeface(txtStatus_user_item.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setUserOnline(String online) {
            ImageView imgOnline = view.findViewById(R.id.imgOnline);
            if (online.equals("true"))
                imgOnline.setVisibility(View.VISIBLE);
            else
                imgOnline.setVisibility(View.INVISIBLE);

        }

        public void setName(String userName) {
            TextView txtName_user_item = view.findViewById(R.id.txtName_user_item);
            txtName_user_item.setText(userName);
        }

        public void setUserImage(String userImage) {
            CircleImageView avatar_user_item = view.findViewById(R.id.avatar_user_item);
            Picasso.get().load(userImage).into(avatar_user_item);
        }
    }
}
