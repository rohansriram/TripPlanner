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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private List<Trip> mdata;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");
    CollectionReference tripRef = db.collection("trips");
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    public static String trip_key ="trip";
    private static final int ADMIN = 0;
    private static final int MEMBER = 1;
    String userID;

    public TripAdapter(List<Trip> mdata, String userID) {
        this.mdata = mdata;
        this.userID= userID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view ;
        ViewHolder viewHolder;

        if(viewType==ADMIN){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item, parent, false);
            viewHolder = new ViewHolder(view);
        }
        else
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item_member, parent, false);
            viewHolder = new ViewHolder(view);
        }

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = mdata.get(position);
        holder.tv_title.setText(trip.tripTitle);
        holder.tv_location.setText( "Latitude:" +trip.getLatitude() +" Longitude: " +trip.getLongitude());
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


    @Override
    public int getItemViewType(int position) {


        if (mdata.get(position).getTripAdminID().equals(userID)) {
            return ADMIN;
        } else {
            return MEMBER;
        }
    }



    public class ViewHolder  extends RecyclerView.ViewHolder{

        TextView tv_title;
        TextView tv_location;
        ImageView trip_coverpic;
        Button bt_removetrip;
        Button bt_discussion;
        Trip newtrip;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            trip_coverpic = itemView.findViewById(R.id.imageView_tripcoverpic);
            bt_removetrip = itemView.findViewById(R.id.button_removetrip);
            bt_discussion = itemView.findViewById(R.id.button_discussion);
            tv_location = itemView.findViewById(R.id.textView_location);

            bt_removetrip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    removeAt(getPosition());
                    String userId = firebaseUser.getUid();
                    if(userId.equals(newtrip.getTripAdminID())){

                        tripRef.document(newtrip.getTripID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                    }
                    else{
                      //  washingtonRef.update("regions", FieldValue.arrayRemove("east_coast"));
                        tripRef.document(newtrip.getTripID()).update("tripMembers", FieldValue.arrayRemove(firebaseUser.getUid()));

                    }

                }
            });


            bt_discussion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(itemView.getContext(),GroupChatActivity.class);
                    intent.putExtra(trip_key,  newtrip.getTripID());
                    itemView.getContext().startActivity(intent);
                }
            });

        }

        public void removeAt(int position){

            mdata.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,mdata.size());
        }
    }
}
