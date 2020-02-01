package com.example.hw07_group1_9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class DiscoverTripsActivity extends AppCompatActivity {


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");
    CollectionReference tripRef = db.collection("trips");
    ArrayList<Trip> tripArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mlayoutManager;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    String userid;
    CircleImageView profile_image;
    TextView username;


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
                startActivity(new Intent(DiscoverTripsActivity.this,MainActivity.class));
                finish();
                return true;

            case R.id.dashboard:
                Intent intent = new Intent(DiscoverTripsActivity.this,NewProfileActivity.class);
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
        setContentView(R.layout.activity_discover_trips);

        androidx.appcompat.widget.Toolbar  toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        profile_image= findViewById(R.id.profile_image);
        username= findViewById(R.id.username);
        auth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.discover_recycler_view);
        firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        userid = firebaseUser.getUid();


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



        tripRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Trip trip = doc.toObject(Trip.class);

                        if (!trip.getTripMembers().contains(userid)) {
                            tripArrayList.add(trip);
                        }
                    }
                    recyclerView.setHasFixedSize(true);
                    mlayoutManager = new LinearLayoutManager(DiscoverTripsActivity.this);
                    recyclerView.setLayoutManager(mlayoutManager);
                    mAdapter = new NewJoinerTripAdapter(tripArrayList);
                    recyclerView.setAdapter(mAdapter);
                    // ViewCompat.setNestedScrollingEnabled(recyclerView,false);
                }
                else {
                    Toast.makeText(DiscoverTripsActivity.this, "There are no new trips to discover!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }
}
