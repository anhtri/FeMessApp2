package com.anhtri.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anhtri.femessapp.R;
import com.anhtri.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference usersDatabase;
    private CircleImageView imgProfileLayout;



    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        final String idCurrentUser = auth.getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Message mess = messageList.get(position);

        if(messageList!=null) {


            final String fromUser = mess.getFrom();

            if (fromUser.equals(idCurrentUser)) {

                holder.txtMessageLayout.setBackgroundColor(Color.WHITE);
                holder.txtMessageLayout.setTextColor(Color.BLACK);
                //holder.txtMessageLayout.setPaddingRelative(65,0,15,0);
                holder.imgProfileLayout.setVisibility(View.INVISIBLE);

            } else {
                databaseReference.child("Users").child(fromUser).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        holder.imgProfileLayout.setVisibility(View.VISIBLE);
                        holder.setUserImage(image);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                holder.txtMessageLayout.setBackgroundResource(R.drawable.message_text_background);
                holder.txtMessageLayout.setTextColor(Color.WHITE);

            }
        }

        holder.txtMessageLayout.setText(mess.getMessage());


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView txtMessageLayout;
        public CircleImageView imgProfileLayout;

        public MessageViewHolder(View itemView) {
            super(itemView);

            txtMessageLayout = itemView.findViewById(R.id.txtMessageLayout);
            imgProfileLayout = itemView.findViewById(R.id.imgProfileLayout);


        }
        public void setUserImage(String image) {
            imgProfileLayout = itemView.findViewById(R.id.imgProfileLayout);

            Picasso.get().load(image).into(imgProfileLayout);

        }
    }
}
