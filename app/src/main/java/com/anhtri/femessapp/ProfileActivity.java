package com.anhtri.femessapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgImageProfile;
    private TextView txtNameProfile, txtStatusProfile, txtFriendCountProfile;
    private Button btnSendRequestProfile, btnDeclineProfile;

    private DatabaseReference userDatabase;
    private DatabaseReference friendRequestDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;
    private FirebaseUser current_user;

    private ProgressDialog progressDialog;



    private String current_state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // get Intent
        final String user_id = getIntent().getStringExtra("user_id");

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Infor");
        progressDialog.setMessage("Please wait while we load the user infor.");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // ----
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
        current_user = FirebaseAuth.getInstance().getCurrentUser();

        //add Control
        imgImageProfile = findViewById(R.id.imgImageProfile);
        txtFriendCountProfile = findViewById(R.id.txtFriendCountProfile);
        txtNameProfile = findViewById(R.id.txtNameProfile);
        txtStatusProfile = findViewById(R.id.txtStatusProfile);
        btnSendRequestProfile = findViewById(R.id.btnSendRequestProfile);
        btnDeclineProfile = findViewById(R.id.btnDeclineProfile);

        //----
        current_state = "not_friend";

        // - -
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                txtNameProfile.setText(name);
                txtStatusProfile.setText(status);

                Picasso.get().load(image).into(imgImageProfile);

                // ----- Friends list / request feature
                friendRequestDatabase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id))
                        {
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (request_type.equals("received")){
                                current_state = "request_received";
                                btnSendRequestProfile.setText("Accept Friend Request");

                                btnDeclineProfile.setVisibility(View.VISIBLE);
                                btnDeclineProfile.setEnabled(true);

                            }else if(request_type.equals("sent")){
                                current_state = "request_sent";
                                btnSendRequestProfile.setText("Cancel Friend Request");

                                btnDeclineProfile.setVisibility(View.INVISIBLE);
                                btnDeclineProfile.setEnabled(false);
                            }

                            progressDialog.dismiss();
                        }else {
                            // cap nhat button o current_id khi da tro thanh friend
                            friendDatabase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id))
                                    {
                                        current_state="friend";
                                        btnSendRequestProfile.setText("Unfriend");

                                        btnDeclineProfile.setVisibility(View.INVISIBLE);
                                        btnDeclineProfile.setEnabled(false);
                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSendRequestProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendRequestProfile.setEnabled(false);

                // Not friend state

                if (current_state.equals("not_friend")){
                    friendRequestDatabase.child(current_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendRequestDatabase.child(user_id).child(current_user.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", current_user.getUid());
                                        notificationData.put("type", "request");

                                        notificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                current_state="request_sent";
                                                btnSendRequestProfile.setText("Cancel Friend Request");

                                                btnDeclineProfile.setVisibility(View.INVISIBLE);
                                                btnDeclineProfile.setEnabled(false);
                                            }
                                        });

                                        //Toast.makeText(ProfileActivity.this,"Request sent successfully",Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }else {
                                Toast.makeText(ProfileActivity.this,"Request sent successfully",Toast.LENGTH_SHORT).show();
                            }
                            btnSendRequestProfile.setEnabled(true);
                        }
                    });

                }

                // Cancel request state
                if(current_state.equals("request_sent")){
                    friendRequestDatabase.child(current_user.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendRequestDatabase.child(user_id).child(current_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    btnSendRequestProfile.setEnabled(true);
                                    current_state="not_friend";
                                    btnSendRequestProfile.setText("Request");

                                    btnDeclineProfile.setVisibility(View.INVISIBLE);
                                    btnDeclineProfile.setEnabled(false);

                                }
                            });

                        }
                    });
                }
                // ---- Req received state
                if(current_state.equals("request_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    friendDatabase.child(current_user.getUid()).child(user_id).child("date").setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendDatabase.child(user_id).child(current_user.getUid()).child("date").setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            friendRequestDatabase.child(current_user.getUid()).child(user_id).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            friendRequestDatabase.child(user_id).child(current_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    btnSendRequestProfile.setEnabled(true);
                                                                    current_state="friend";
                                                                    btnSendRequestProfile.setText("Unfriend");

                                                                    btnDeclineProfile.setVisibility(View.INVISIBLE);
                                                                    btnDeclineProfile.setEnabled(false);

                                                                }
                                                            });

                                                        }
                                                    });

                                        }
                                    });
                        }
                    });

                }

            }
        });

    }
}
