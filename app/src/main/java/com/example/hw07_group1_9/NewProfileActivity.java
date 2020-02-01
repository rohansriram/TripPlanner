package com.example.hw07_group1_9;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewProfileActivity extends AppCompatActivity {


    CircleImageView profile_image;
    TextView username;
    FirebaseUser firebaseUser;
    Button button_discoverpeople;
    Button button_createTrip;
    Button button_mytrips;
    Button button_discover_trips;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");
    CollectionReference tripRef = db.collection("trips");
    ArrayList<String> userNames = new ArrayList<>();
    UserProfile edituserprofile;


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
                startActivity(new Intent(NewProfileActivity.this,MainActivity.class));
                finish();
                return true;
            case R.id.edit:
                Intent intent = new Intent(NewProfileActivity.this,EditProfileActivity.class);
                intent.putExtra("key",edituserprofile);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);

        androidx.appcompat.widget.Toolbar  toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image= findViewById(R.id.profile_image);
        username= findViewById(R.id.username);
        button_discoverpeople=findViewById(R.id.button_discoverpeople);
        button_createTrip = findViewById(R.id.button_createtrip);
        button_mytrips = findViewById(R.id.button_mytrips);
        button_discover_trips = findViewById(R.id.button_discovertrips);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();


        userRef.document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    edituserprofile =userProfile;
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
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });



        button_discoverpeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final CharSequence[] usersAlert = userNames.toArray(new CharSequence[userNames.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(NewProfileActivity.this);
                builder.setTitle("Your Friends");
                builder.setItems(usersAlert, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        button_createTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(NewProfileActivity.this,CreateTripActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });



        button_mytrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(NewProfileActivity.this,MyTripsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
               finish();

            }
        });



        button_discover_trips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewProfileActivity.this,DiscoverTripsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
               finish();
            }
        });




//        tripRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//
//                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                    Trip trip = doc.toObject(Trip.class);
//                    tripArrayList.add(trip);
//                }
//                recyclerView.setHasFixedSize(true);
//                mlayoutManager = new LinearLayoutManager(NewProfileActivity.this);
//                recyclerView.setLayoutManager(mlayoutManager);
//                mAdapter = new TripAdapter(tripArrayList);
//                recyclerView.setAdapter(mAdapter);
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });


    }


}
