package com.example.hw07_group1_9;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewJoinerTripAdapter  extends RecyclerView.Adapter<NewJoinerTripAdapter.ViewHolder>{

    private List<Trip> mdata;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");
    CollectionReference tripRef = db.collection("trips");
    FirebaseAuth auth;
    FirebaseUser firebaseUser;


    public NewJoinerTripAdapter(List<Trip> mdata) {
        this.mdata = mdata;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.discover_trip_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = mdata.get(position);
        holder.tv_title.setText(trip.tripTitle);
        String image = trip.getImageURL();
        if(image.equals("default")){
            holder.trip_coverpic.setImageResource(R.mipmap.ic_launcher);
        }else{
            Picasso.get().load(image).into(holder.trip_coverpic);
        }

        holder.newtrip = trip;
    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }


    public class ViewHolder  extends RecyclerView.ViewHolder{

        TextView tv_title;
        ImageView trip_coverpic;
        Button join;
        Trip newtrip;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            trip_coverpic = itemView.findViewById(R.id.imageView_tripcoverpic);
            join = itemView.findViewById(R.id.button_joinnew);

            join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                 tripRef.document(newtrip.getTripID()).update("tripMembers", FieldValue.arrayUnion(firebaseUser.getUid()))
                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                 Intent intent = new Intent(itemView.getContext(),MyTripsActivity.class);
                                 itemView.getContext().startActivity(intent);
                             }
                         }).addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {

                     }
                 });
                }
            });

        }

    }
}
