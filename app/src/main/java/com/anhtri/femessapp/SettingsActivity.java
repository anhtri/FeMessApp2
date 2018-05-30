package com.anhtri.femessapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {


    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;

    private CircleImageView avatar_setting;
    private TextView display_name_setting, status_setting;
    private Button btnChangeAvatar, btnChangeStatus;

    private static final int GALERY_PICK = 2708;

    //Storage Firebase
    private StorageReference imageStorage;

    //Progress
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        avatar_setting = findViewById(R.id.avatar_setting);
        display_name_setting = findViewById(R.id.display_name_setting);
        status_setting = findViewById(R.id.status_setting);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnChangeStatus = findViewById(R.id.btnChangeStatus);

        //Storage Firebase
        imageStorage = FirebaseStorage.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = currentUser.getUid();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        userDatabase.keepSynced(true);

        //----------------Change avatar----------------------
        btnChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent galeryIntent = new Intent();
                galeryIntent.setType("image/*");
                galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galeryIntent, "SELECT IMAGE"), GALERY_PICK);

                // start picker to get image for cropping and then use the image in cropping activity
                //CropImage.activity()
                //       .setGuidelines(CropImageView.Guidelines.ON)
                //       .start(SettingsActivity.this);


            }
        });

        //-----------------Xu ly Change Status--------------------
        btnChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = status_setting.getText().toString();
                Intent changeStatusIntent = new Intent(SettingsActivity.this, ChangeStatusActivity.class);
                changeStatusIntent.putExtra("STATUS", status);
                startActivity(changeStatusIntent);
            }
        });
        //-----------------------------------------------------------


        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                display_name_setting.setText(name);
                status_setting.setText(status);

                if (!image.equals("default"))
                {
                    Picasso.get().load(image).placeholder(R.drawable.no_avatar).into(avatar_setting);
                }


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Crop image
        if (requestCode == GALERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);


            //String imageUri = data.getDataString();
            //Toast.makeText(SettingsActivity.this, imageUri,Toast.LENGTH_LONG).show();

        }

        //Up image cropped to Storage
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){

                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading image...");
                progressDialog.setMessage("Please wait while we upload and process the image");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filepath = new File(resultUri.getPath());

                String currentUser_uid = currentUser.getUid();

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //https://firebase.google.com/docs/storage/android/upload-files
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                final StorageReference filePath = imageStorage.child("profile_image").child(random()+".jpg");
                final StorageReference thumb_filePath = imageStorage.child("profile_image").child("thumbs").child(currentUser_uid+".jpg");



                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    //Lay Url cua thumb file trong thu muc thump_image
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful())
                                    {
                                        //update thump_image và image vao hashMap vào storage
                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put("image",download_url);
                                        update_hashMap.put("thumb_image",thumb_downloadUrl);

                                        userDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this,"Success Update",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(SettingsActivity.this,"Error upload thumbnail",Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SettingsActivity.this,"Error upload thumbnail",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                        
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
              Exception error = result.getError();
            }
        }
    }

    //Random name of image uploaded
    public static String random(){
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i=0; i<randomLength;i++)
        {
            tempChar = (char) (generator.nextInt(96)+32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    
}
