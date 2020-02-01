package com.example.hw07_group1_9;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {


    CircleImageView profile_image;
    TextView username;
    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");
    CollectionReference tripRef = db.collection("trips");
    TextView txt_send;
    ImageButton button_send;
    ImageButton button_image;
    RecyclerView message_recyclerview;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mlayoutManager;
    String messageSender;
    String messageSentTime;
    String messageSentDate;
    String tripGroupID;
    ArrayList<Message> messageArrayList = new ArrayList<>();
    String image ="default";
    CollectionReference chatRef = db.collection("chats");
    String userID;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri=null;
    private StorageReference mStorageRef;
    Bitmap upload_bitmap= null;
    String imageURL = "default";
    int flag;


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
                startActivity(new Intent(GroupChatActivity.this,MainActivity.class));
                finish();
                return true;

            case R.id.dashboard:
                Intent intent = new Intent(GroupChatActivity.this,NewProfileActivity.class);
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
        setContentView(R.layout.activity_group_chat);

        androidx.appcompat.widget.Toolbar  toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image= findViewById(R.id.profile_image);
        username= findViewById(R.id.username);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        txt_send = findViewById(R.id.text_send);
        button_send= findViewById(R.id.btn_send);
        button_image=findViewById(R.id.imagebuttonchat);
        message_recyclerview= findViewById(R.id.group_recyclerview);
        message_recyclerview.setHasFixedSize(true);
        mlayoutManager = new LinearLayoutManager(GroupChatActivity.this);
      // mlayoutManager.setReverseLayout(true);
        message_recyclerview.setLayoutManager(mlayoutManager);
        userID = firebaseUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");


        userRef.document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    Log.d("demo", "onDataChange: " +userProfile.getFirstname());
                    username.setText(userProfile.getFirstname()+" " +userProfile.getLastname());
                    messageSender = userProfile.getFirstname()+" " +userProfile.getLastname();
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


        if(getIntent()!= null && getIntent().getExtras()!=null){
            tripGroupID = getIntent().getExtras().getString(TripAdapter.trip_key);
        }



     //  showChatMessages();
        showMessages();


        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (txt_send.getText().toString().isEmpty()) {
                    Toast.makeText(GroupChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                } else {
                    savemessage();
                }

            }
        });


        button_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_REQUEST);


            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            mImageUri = data.getData();
            Log.d("demo", "onActivityResult: imageURL " +mImageUri);
            try {
                upload_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            new asyncSaveImage().execute();

        }
    }


    private void savemessage() {

            String message = txt_send.getText().toString();
            txt_send.setText("");
            button_send.setEnabled(false);
            Calendar calDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            messageSentDate = currentDateFormat.format(calDate.getTime());

            Calendar calTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            messageSentTime = currentTimeFormat.format(calTime.getTime());


          String messageID = chatRef.document(tripGroupID).collection("messages").document().getId();

         final Message message1 = new Message(messageSender,messageSentTime,image,message,messageSentDate,firebaseUser.getUid(),messageID);


        chatRef.document(tripGroupID).collection("messages").document(messageID).set(message1)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            txt_send.setEnabled(true);
                            button_send.setEnabled(true);
                            if(flag==1){
                                messageArrayList.add(message1);
                                mAdapter = new MessageAdapter(messageArrayList, userID,tripGroupID);
                                message_recyclerview.setAdapter(mAdapter);
                                flag=2;
                            }else{
                                messageArrayList.add(message1);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        txt_send.setEnabled(true);
                        button_send.setEnabled(true);
                        Log.d("Demo", "onFailure: " +e.getMessage());

                    }
                });
    }


//    private void showChatMessages() {
//
//        chatRef.document(tripGroupID).collection("messages").orderBy("date").orderBy("time")
//                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                        if (e != null) {
//                            Log.e("ChatRoomActivity", "Listen failed.", e);
//                            return;
//                        }
//
//                        messageArrayList.clear();
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                                Message message2 = doc.toObject(Message.class);
//                                messageArrayList.add(message2);
//                            }
//                            mAdapter = new MessageAdapter(messageArrayList, userID,tripGroupID);
//                            message_recyclerview.setAdapter(mAdapter);
//                        }
//                        else{
//                            Toast.makeText(GroupChatActivity.this, "No messages to display", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }


    private void showMessages(){

        chatRef.document(tripGroupID).collection("messages").orderBy("date").orderBy("time").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Message message2 = doc.toObject(Message.class);
                                messageArrayList.add(message2);
                            }
                            mAdapter = new MessageAdapter(messageArrayList, userID,tripGroupID);
                            message_recyclerview.setAdapter(mAdapter);
                           // ViewCompat.setNestedScrollingEnabled(message_recyclerview,false);
                        }
                        else{
                            Toast.makeText(GroupChatActivity.this, "No messages to display", Toast.LENGTH_SHORT).show();
                            flag=1;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("demo", "onFailure: " +e.getMessage());
            }
        });
    }



    private  class asyncSaveImage extends AsyncTask<Void, Void , Void>{


        @Override
        protected Void doInBackground(Void... voids) {

            final String message = "default";
            Calendar calDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            messageSentDate = currentDateFormat.format(calDate.getTime());

            Calendar calTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            messageSentTime = currentTimeFormat.format(calTime.getTime());


            final StorageReference imageRepo = mStorageRef.child(System.currentTimeMillis() + ".png");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            upload_bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask  mUploadTask = imageRepo.putBytes(data);

            Task<Uri> urlTask = mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

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
                        String messageID = chatRef.document(tripGroupID).collection("messages").document().getId();
                        final Message message1 = new Message(messageSender,messageSentTime,imageURL,message,messageSentDate,firebaseUser.getUid(),messageID);

                        chatRef.document(tripGroupID).collection("messages").document(messageID).set(message1)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            if(flag==1){
                                                messageArrayList.add(message1);
                                                mAdapter = new MessageAdapter(messageArrayList, userID,tripGroupID);
                                                message_recyclerview.setAdapter(mAdapter);
                                                flag=2;
                                            }else{
                                                messageArrayList.add(message1);
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Demo", "onFailure: check this!!!!!!!!! " +e.getMessage());
                                    }
                                });
                    }
                }
            });

            return null;
        }

    }

}
