package com.example.hw07_group1_9;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText et_firstname;
    EditText et_lastname;
    EditText et_email;
    EditText et_password;
    RadioGroup radioGroup;
    RadioButton rb_female;
    RadioButton rb_male;
    Button button_register;
    ImageView iv_selectavatr;
    TextView textView_photo;
    String gender ="female";
    String avatar ="default";
    FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri =null;
    private StorageReference mStorageRef;
    Bitmap upload_bitmap= null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_firstname =findViewById(R.id.et_firstname);
        et_lastname=findViewById(R.id.et_lastname);
        et_email=findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        radioGroup = findViewById(R.id.radiogroup);
        rb_female = findViewById(R.id.rb_female);
        rb_male = findViewById(R.id.rb_male);
        iv_selectavatr=findViewById(R.id.iv_selectavatar);
        button_register = findViewById(R.id.button_register);
        textView_photo= findViewById(R.id.textView4);

        auth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");


        textView_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        rb_female.setChecked(true);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.rb_female){
                    gender = "female";
                }
                else if(i==R.id.rb_male){
                    gender = "male";
                }
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String firstname = et_firstname.getText().toString();
                String lastname= et_lastname.getText().toString();
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();


                if(TextUtils.isEmpty(firstname)||TextUtils.isEmpty(lastname)||TextUtils.isEmpty(email)||TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
                else if (password.length()<6){
                    Toast.makeText(RegisterActivity.this, "Must be at least 6 characters", Toast.LENGTH_SHORT).show();
                }else{
                    register(firstname,lastname,email,password,gender);
                }

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            try {
                upload_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                iv_selectavatr.setImageBitmap(upload_bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void register(final String firstname , final String lastname , final String email, final String password , final String gender) {


        if(mImageUri!=null){
            final StorageReference imageRepo = mStorageRef.child(System.currentTimeMillis() + ".png");

            //  Converting the Bitmap into a bytearrayOutputstream....
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            upload_bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask  mUploadTask = imageRepo.putBytes(data);

            Task<Uri> urlTask = mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    //                return null;
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    Log.d("demo", imageRepo.getDownloadUrl().toString());
                    return imageRepo.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Log.d("demo", "Image Download URL" + task.getResult());
                        avatar = task.getResult().toString();

                        auth.createUserWithEmailAndPassword(email,password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            FirebaseUser firebaseUser = auth.getCurrentUser();
                                            assert firebaseUser != null;
                                            String userid = firebaseUser.getUid();
                                            UserProfile userProfile = new UserProfile(firstname,lastname,userid,gender,avatar);

                                            userRef.document(userid).set(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        goTo();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                        }else{
                                            Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                    }
                }
            });

        }
        else {

            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                assert firebaseUser != null;
                                String userid = firebaseUser.getUid();
                                UserProfile userProfile = new UserProfile(firstname,lastname,userid,gender,avatar);

                                userRef.document(userid).set(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                              goTo();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }else{
                                Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

    }

    private void goTo(){

        Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this,NewProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }



    }




