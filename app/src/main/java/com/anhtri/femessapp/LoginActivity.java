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

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail_Login,edtPass_Login;
    private Button btnLogin;
    private FirebaseAuth mAuth;

    private Toolbar login_toolbar;

    //ProgressDialog
    private ProgressDialog login_Progres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(login_toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ProgressDialog
        login_Progres = new ProgressDialog(this);

        edtEmail_Login = findViewById(R.id.edtEmail_Login);
        edtPass_Login = findViewById(R.id.edtPass_Login);
        btnLogin =findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail_Login.getText().toString();
                String password = edtPass_Login.getText().toString();
                if (!email.isEmpty() || !password.isEmpty()) {

                    login_Progres.setTitle("Login User...");
                    login_Progres.setMessage("Please wait while we login your account !!!");
                    login_Progres.setCanceledOnTouchOutside(false);
                    login_Progres.show();
                    loginUser(email, password);
                }
                else{
                    Toast.makeText(LoginActivity.this, "Check the form, please!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            login_Progres.dismiss();
                            // Sign in success, update UI with the signed-i
                            Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                        else {
                            login_Progres.hide();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
