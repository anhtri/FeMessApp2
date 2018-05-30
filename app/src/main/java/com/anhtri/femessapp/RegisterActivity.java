package com.anhtri.femessapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail_Register,edtPass_Register,edtName_Register;
    private Button btnCreate_Acc_Register;

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    private DatabaseReference myRef;

    private Toolbar register_toolbar;

    //ProgressDialog
    private ProgressDialog register_Progres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar set
        register_toolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(register_toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //back lai active truoc

        //ProgressDialog
        register_Progres = new ProgressDialog(this);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();



        //Android field
        edtEmail_Register = findViewById(R.id.edtEmail_Register);
        edtName_Register = findViewById(R.id.edtName_Register);
        edtPass_Register = findViewById(R.id.edtPass_Register);
        btnCreate_Acc_Register = findViewById(R.id.btnCreate_Acc_Register);



        btnCreate_Acc_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail_Register.getText().toString();
                String password = edtPass_Register.getText().toString();
                String name = edtName_Register.getText().toString();

                if(!email.isEmpty() || !password.isEmpty() || !name.isEmpty())
                {
                    register_Progres.setTitle("Registering User...");
                    register_Progres.setMessage("Please wait while we create your account !!!");
                    register_Progres.setCanceledOnTouchOutside(false);
                    register_Progres.show();
                    register_user(name, email, password);
                }else
                {
                    Toast.makeText(RegisterActivity.this, "Check the form, please!!!",Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void register_user(final String name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            assert currentUser != null;
                            String uid = currentUser.getUid();

                            //Firebase database
                            database = FirebaseDatabase.getInstance();
                            myRef =database.getReference().child("Users").child(uid);

                            //Hash map
                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("status","Hi there.I'm using FeMess chat app.");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            myRef.setValue(userMap);

                            register_Progres.dismiss();
                            Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        } else {
                            register_Progres.hide();
                            Toast.makeText(RegisterActivity.this,"Can't Sign In. Please check the form and try again!!!",Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    };
}
