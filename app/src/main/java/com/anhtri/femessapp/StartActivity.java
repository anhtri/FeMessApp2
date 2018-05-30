package com.anhtri.femessapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    private Button btnRegister_Start,btnLogin_Start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnRegister_Start = findViewById(R.id.btnRegister_Start);
        btnLogin_Start = findViewById(R.id.btnLogin_Start);

        btnLogin_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_intent = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(login_intent);
                //finish();
            }
        });
        btnRegister_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(register_intent);
                //finish();
            }
        });
    }
}
