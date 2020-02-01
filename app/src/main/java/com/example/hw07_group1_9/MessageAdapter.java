package com.example.hw07_group1_9;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mdata;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userRef = db.collection("users");
    CollectionReference tripRef = db.collection("trips");
    CollectionReference chatRef = db.collection("chats");
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    private static final int SENT = 0;
    private static final int RECEIVED = 1;
    String userID;
    String tripID;

    public MessageAdapter(List<Message> mdata , String userID,String tripID) {
        this.mdata = mdata;
        this.userID = userID;
        this.tripID = tripID;
        Log.d("demo", "onEvent: check userID " +userID);
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

         View view;
         ViewHolder viewHolder;

        if (viewType == SENT) {

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_sent, parent, false);
            viewHolder = new ViewHolder(view);

        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
            viewHolder = new ViewHolder(view);
        }

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

       Message message = mdata.get(position);
        holder.sender.setText(message.getSenderName());
//        holder.message.setText(message.getMsgContent());
       holder.time.setText(message.getTime());
       holder.messageobj= message;

       if( (!message.getImage().equals("default")) && message.getMsgContent().equals("default") ){
           holder.imageView.setVisibility(View.VISIBLE);
           holder.message.setVisibility(View.INVISIBLE);
           Picasso.get().load(message.getImage()).into(holder.imageView);
       }
       else{
           holder.message.setText(message.getMsgContent());
       }

    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }

    @Override
    public int getItemViewType(int position) {


        if (mdata.get(position).getSenderID().equals(userID)) {
            return SENT;
        } else {
            return RECEIVED;
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView sender , message , time;
        ImageView imageView;
        Message messageobj;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            sender = itemView.findViewById(R.id.sender_name);
            message = itemView.findViewById(R.id.messagechat);
            time = itemView.findViewById(R.id.timechat);
            imageView = itemView.findViewById(R.id.imageView_chat);
           
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                   if(messageobj.getSenderID().equals(userID)) {

                       removeAt(getPosition());

                       chatRef.document(tripID).collection("messages").
                               document(messageobj.getMessageID()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()) {
                                   Toast.makeText(itemView.getContext(), "Message Deleted!", Toast.LENGTH_SHORT).show();
                               }
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {

                           }
                       });
                   }else{
                       Toast.makeText(itemView.getContext(), "You can delete only your message!", Toast.LENGTH_SHORT).show();
                   }

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
