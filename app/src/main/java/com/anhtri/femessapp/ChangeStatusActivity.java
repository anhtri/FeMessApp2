package com.anhtri.femessapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeStatusActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar change_status_appbar;

    private TextInputLayout status_input;
    private Button btnSaveChangeStatus;

    private DatabaseReference statusDatabase;
    private FirebaseUser currentUser;

    //Progress Dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);


        btnSaveChangeStatus = findViewById(R.id.btnSaveChangeStatus);
        status_input = findViewById(R.id.status_input);

        String status = getIntent().getStringExtra("STATUS");
        status_input.getEditText().setText(status);

        //Toolbar set
        change_status_appbar = findViewById(R.id.change_status_appbar);
        setSupportActionBar(change_status_appbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //back lai active truoc

        //Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = currentUser.getUid();
        statusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);




        btnSaveChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progress
                progressDialog =new ProgressDialog(ChangeStatusActivity.this);
                progressDialog.setTitle("Saving change...");
                progressDialog.setMessage("Please wait while we're saving your change!");
                progressDialog.show();


                String status = status_input.getEditText().getText().toString();

                //Change stt
                statusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            Toast.makeText(ChangeStatusActivity.this,"Change successfully!",Toast.LENGTH_SHORT).show();
                            /*Intent settingIntent = new Intent(ChangeStatusActivity.this,SettingsActivity.class);
                            //settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(settingIntent);
                            finish();*/
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"There are some errors in saving status.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }
}
