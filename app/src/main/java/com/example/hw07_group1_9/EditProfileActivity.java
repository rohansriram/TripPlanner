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
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    UserProfile userProfile;
    EditText et_firstname;
    EditText et_lastname;
    RadioGroup radioGroup;
    RadioButton rb_female;
    RadioButton rb_male;
    Button button_register;
    ImageView iv_selectavatr;
    TextView textView_photo;
    String gender ="female";
    String avatar;
    FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri=null;
    private StorageReference mStorageRef;
    CircleImageView profile_image;
    TextView username;
    FirebaseUser firebaseUser;
    Bitmap upload_bitmap= null;
    String imageURL = "default";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(EditProfileActivity.this,MainActivity.class));
                finish();
                return true;

            case R.id.dashboard:
                Intent intent = new Intent(EditProfileActivity.this,NewProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        androidx.appcompat.widget.Toolbar  toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image= findViewById(R.id.profile_image);
        username= findViewById(R.id.username);
        et_firstname =findViewById(R.id.et_firstnameedit);
        et_lastname=findViewById(R.id.et_lastnameedit);
        radioGroup = findViewById(R.id.radiogroupedit);
        rb_female = findViewById(R.id.rb_femaleedit);
        rb_male = findViewById(R.id.rb_maleedit);
        iv_selectavatr=findViewById(R.id.iv_editavatr);
        button_register = findViewById(R.id.button_upadteprofile);
        textView_photo= findViewById(R.id.editprofile);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        userRef.document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    String displaynme =userProfile.getFirstname()+" " +userProfile.getLastname();
                    username.setText(displaynme);
                    String image = userProfile.getImage();
                    if(image.equals("default")){
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    }else{
                        Picasso.get().load(image).into(profile_image);
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });



        if(getIntent()!=null && getIntent().getExtras()!=null){

            userProfile =(UserProfile) getIntent().getExtras().getSerializable("key");

            et_firstname.setText(userProfile.getFirstname());
            et_lastname.setText(userProfile.getLastname());
            String image = userProfile.getImage();
            if(image.equals("default")){
                iv_selectavatr.setImageResource(R.mipmap.ic_launcher);
            }else{
                Picasso.get().load(image).into(iv_selectavatr);
            }
           if(userProfile.getGender().equals("female")){
               rb_female.setChecked(true);
           }else{
               rb_male.setChecked(true);
           }

        }


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

                if(TextUtils.isEmpty(firstname)||TextUtils.isEmpty(lastname)){
                    Toast.makeText(EditProfileActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
                else{
                    register(firstname,lastname,gender);
                }

            }
        });

    }

    private void register(final String firstname, final String lastname, final String gender) {


        avatar = userProfile.getImage();

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
                        imageURL = task.getResult().toString();
                        userRef.document(userProfile.getUserID()).update("firstname",firstname,"lastname",lastname, "gender",gender,"image",imageURL).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                               goTo();
                            }
                        });

                    }
                }
            });

        }
        else {
            userRef.document(userProfile.getUserID()).update("firstname",firstname,"lastname",lastname,
                    "gender",gender,"image",avatar).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    startActivity(new Intent(EditProfileActivity.this,NewProfileActivity.class));
                    finish();
                }
            });
        }

    }


    private void goTo(){
        Toast.makeText(EditProfileActivity.this, "Successfully updated profile", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(EditProfileActivity.this,NewProfileActivity.class));
        finish();
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

}
