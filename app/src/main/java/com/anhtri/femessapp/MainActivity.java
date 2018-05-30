package com.anhtri.femessapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar main_page_toolbar;

    private ViewPager main_tabPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private TabLayout main_tabs;

    private DatabaseReference userReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //
        main_page_toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(main_page_toolbar);
        getSupportActionBar().setTitle("FeMess App");

        //tro den Users/Current_uid
        //userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        if (mAuth.getCurrentUser() != null) {


            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }
        //Tabs
        main_tabPager = findViewById(R.id.main_tabPager);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        main_tabPager.setAdapter(sectionsPagerAdapter);

        main_tabs = findViewById(R.id.main_tabs);
        main_tabs.setupWithViewPager(main_tabPager);

    }



    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendToStartActivity();
        } else {
            userReference.child("online").setValue(true);
        }

    }
    //-----set false when onStop
    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null) {
            userReference.child("online").setValue(false);

        }
    }

    private void sendToStartActivity() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout)
        {
            FirebaseAuth.getInstance().signOut();
            userReference.child("online").setValue(false);
            sendToStartActivity();
        }

        if(item.getItemId() == R.id.main_account_settings)
        {
            Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingIntent);
        }
        if (item.getItemId() == R.id.main_all_users)
        {
            Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(usersIntent);
        }
        return true;
    }
}
