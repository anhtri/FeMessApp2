package com.anhtri.femessapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.anhtri.adapter.MessageAdapter;
import com.anhtri.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private Toolbar chat_appbar;
    private TextView txtTitleView;
    private TextView txtLastSeen;
    private CircleImageView imgChatUser;
    private ImageButton btnAdd_chat;
    private ImageButton btnSend_chat;
    private EditText edtMessage_chat;

    private DatabaseReference databaseReference;
    private String idChatUser;
    private FirebaseAuth currentUser;
    private String idCurrentUser;

    private RecyclerView recyclerMessages;
    private SwipeRefreshLayout layoutMessageSwipe;
    private final List<com.anhtri.model.Message> messageList =new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter  messageAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;

    /*New solution*/
    private int itemPos =0;
    private String lastKey = "";
    private String prevKey = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //configure control
        edtMessage_chat = findViewById(R.id.edtMessage_chat);
        btnAdd_chat = findViewById(R.id.btnAdd_chat);
        btnSend_chat = findViewById(R.id.btnSend_chat);




        //configure toolbar
        chat_appbar = findViewById(R.id.chat_appbar);
        setSupportActionBar(chat_appbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance();
        idCurrentUser = currentUser.getCurrentUser().getUid();

        //get name chat user
        idChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");
        //getSupportActionBar().setTitle(idChatUser);

        //Custom tool bar
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);


        /* Custom Actionbar items */
        txtTitleView = findViewById(R.id.txtTitleView);
        txtLastSeen = findViewById(R.id.txtLastSeen);
        imgChatUser = findViewById(R.id.imgChatUser);

        //config Display message
        messageAdapter = new MessageAdapter(messageList);
        recyclerMessages = findViewById(R.id.listMessages);
        layoutMessageSwipe = findViewById(R.id.layoutMessageSwipe);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerMessages.setHasFixedSize(true);

        recyclerMessages.setLayoutManager(linearLayoutManager);
        recyclerMessages.setAdapter(messageAdapter);


        loadMessages();

        /**
         * Action bar display
         */
        txtTitleView.setText(userName);
        databaseReference.child("Users").child(idChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                Picasso.get().load(image).into(imgChatUser);
                if(online.equals("true")){

                    txtLastSeen.setText("Online");

                }else {

                    txtLastSeen.setText("Offline");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /***
         * Create Chat folder on Realtime database
         */
        databaseReference.child("Chat").child(idCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(idChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + idCurrentUser + "/" + idChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + idChatUser + "/" + idCurrentUser, chatAddMap);
                    
                    databaseReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null){

                                Log.d("CHAT_LOG",databaseError.getMessage().toString());

                            }

                        }
                    });
                };

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /********************
         * Send Button Event
         * ******************/
        btnSend_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        /**
         * Refresh Layout
         */
        layoutMessageSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                currentPage++;

                itemPos = 0;

                loadMoreMessages();

            }
        });
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = databaseReference.child("messages").child(idCurrentUser).child(idChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);
                String messageKey = dataSnapshot.getKey();

                if (!prevKey.equals(messageKey)){
                    messageList.add(itemPos++, message);
                }else {
                    prevKey = messageKey;
                }

                if (itemPos == 1){
                    lastKey = messageKey;
                }




                Log.d("TOTALKEYS", "Last key : " + lastKey + " | Prev Key : " + prevKey + " | Message key : " + messageKey );
                messageAdapter.notifyDataSetChanged();

                layoutMessageSwipe.setRefreshing(false);

                linearLayoutManager.scrollToPositionWithOffset(10,0);
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

    }

    private void loadMessages() {

        DatabaseReference messageRef = databaseReference.child("messages").child(idCurrentUser).child(idChatUser);
        Query messageQuery = messageRef.limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);


                itemPos++;
                if (itemPos == 1){
                    String messageKey = dataSnapshot.getKey();
                    lastKey = messageKey;
                    prevKey = messageKey;
                }

                messageList.add(message);
                messageAdapter.notifyDataSetChanged();

                recyclerMessages.scrollToPosition(messageList.size()-1);

                layoutMessageSwipe.setRefreshing(false);

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


    }


    /**
     * Method: send message when click button send
     */
    private void sendMessage() {

        String message = edtMessage_chat.getText().toString();

        if(!TextUtils.isEmpty(message)){

            /* Push id of message*/
            DatabaseReference user_message_push = databaseReference.child("messages").child(idCurrentUser).child(idChatUser).push();

             String push_id = user_message_push.getKey();



            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",idCurrentUser);

            edtMessage_chat.setText("");

            databaseReference.child("messages").child(idCurrentUser).child(idChatUser).child(push_id).updateChildren(messageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null){

                        Log.d("CHAT_LOG",databaseError.getMessage().toString());

                    }
                }
            });

            databaseReference.child("messages").child(idChatUser).child(idCurrentUser).child(push_id).updateChildren(messageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null){

                        Log.d("CHAT_LOG",databaseError.getMessage().toString());

                    }
                }
            });
        }

    }
    /*-----------------------------end method---------------------------*/
}
