package com.example.hw07_group1_9;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ContentResolver;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateTripActivity extends AppCompatActivity {

    EditText et_triptitle;
    EditText et_latitude;
    EditText et_longitude;
    ImageView iv_tripphoto;
    Button button_create;
    FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");
    CollectionReference tripRef = db.collection("trips");
    ArrayList<String> tripMembers = new ArrayList<>();
    CircleImageView profile_image;
    TextView username;
    FirebaseUser firebaseUser;
    Button addusers;
    ArrayList<String> userNames = new ArrayList<>();
    ArrayList<String> selectedfriendId = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri = null;
    private StorageReference mStorageRef;
    Bitmap upload_bitmap= null;
    String imageURL = "default";
    String tripid;
    Trip trip;

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
                startActivity(new Intent(CreateTripActivity.this,MainActivity.class));
                finish();
                return true;

            case R.id.dashboard:
                Intent intent = new Intent(CreateTripActivity.this,NewProfileActivity.class);
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
        setContentView(R.layout.activity_create_trip);


        androidx.appcompat.widget.Toolbar  toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        et_triptitle = findViewById(R.id.et_triptitle);
        et_latitude = findViewById(R.id.et_latitude);
        et_longitude = findViewById(R.id.et_longitutde);
        iv_tripphoto= findViewById(R.id.imageView);
        button_create= findViewById(R.id.button_create);
        profile_image= findViewById(R.id.profile_image);
        username= findViewById(R.id.username);
        addusers= findViewById(R.id.button_addusers);

        auth = FirebaseAuth.getInstance();
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        iv_tripphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_REQUEST);

            }
        });


        userRef.document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    Log.d("demo", "onDataChange: " +userProfile.getFirstname());
                    username.setText(userProfile.getFirstname()+" " +userProfile.getLastname());
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


        userRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    UserProfile userProfile = doc.toObject(UserProfile.class);
                    if(!userProfile.getUserID().equals(firebaseUser.getUid())) {
                        String name = userProfile.getFirstname()+" " +userProfile.getLastname();
                        userNames.add(name);
                        selectedfriendId.add(userProfile.getUserID());
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        addusers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final CharSequence[] usersAlert = userNames.toArray(new CharSequence[userNames.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateTripActivity.this);
                builder.setTitle("Choose Friends to add ");
                builder.setMultiChoiceItems(usersAlert, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                        if(b){
                            String selectID = selectedfriendId.get(i);
                            tripMembers.add(selectID);
                            Log.d("demo", "onClick:selected friends " +userNames.get(i));
                        }
                        else{
                            String selectID = selectedfriendId.get(i);
                            tripMembers.remove(selectID);
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.cancel();

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



        button_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String triptitle = et_triptitle.getText().toString();
                String latitude= et_latitude.getText().toString();
                String longitude = et_longitude.getText().toString();

                if(TextUtils.isEmpty(triptitle)||TextUtils.isEmpty(latitude)||TextUtils.isEmpty(longitude)){
                    Toast.makeText(CreateTripActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
                else{

                    createTrip(triptitle,latitude,longitude);
                }

            }
        });

    }

    private void createTrip(final String triptitle, final String latitude, final String longitude) {

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

                        firebaseUser = auth.getCurrentUser();
                        assert firebaseUser != null;
                        String userid = firebaseUser.getUid();
                        tripMembers.add(userid);
                        tripid = tripRef.document().getId();
                        trip = new Trip(tripid,userid,triptitle,imageURL,latitude,longitude,tripMembers);
                        Log.d("demo", "createTrip: " + triptitle);


                        tripRef.document(tripid).set(trip).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                    }
                }
            });

        }
        else {
            Toast.makeText(CreateTripActivity.this, "No file selected  as trip photo!", Toast.LENGTH_SHORT).show();
            firebaseUser = auth.getCurrentUser();
            assert firebaseUser != null;
            String userid = firebaseUser.getUid();
            tripMembers.add(userid);
            tripid = tripRef.document().getId();
            trip = new Trip(tripid,userid,triptitle,imageURL,latitude,longitude,tripMembers);
            Log.d("demo", "createTrip: " + triptitle);


            tripRef.document(tripid).set(trip).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }

        //        String id = db.collection("collection_name").document().getId();
       //        db.collection("collection_name").document(id).set(object);

    }


   private  void goTo(){

       Toast.makeText(this, "Trip successfully created!Returning to dashboard", Toast.LENGTH_SHORT).show();
       Intent intent = new Intent(CreateTripActivity.this,NewProfileActivity.class);
       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
       startActivity(intent);
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
                 iv_tripphoto.setImageBitmap(upload_bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
