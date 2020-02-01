package com.example.hw07_group1_9;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

   private Context mcontext;
   private List<UserProfile> musers;

    public UserAdapter(Context mcontext, List<UserProfile> musers) {
        this.mcontext = mcontext;
        this.musers = musers;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        UserProfile userProfile = musers.get(position);
        holder.username.setText(userProfile.firstname+ " " +userProfile.lastname);
        String image = userProfile.getImage();
        switch (image){

//            case "avatar_f1":
//              holder.profile_image.setImageResource(R.drawable.avatar_f_1);
//                break;
//
//            case "avatar_f2":
//                holder.profile_image.setImageResource(R.drawable.avatar_f_2);
//                break;
//
//            case "avatar_f3":
//                holder.profile_image.setImageResource(R.drawable.avatar_f_3);
//                break;
//
//            case "avatar_m1":
//                holder.profile_image.setImageResource(R.drawable.avatar_m_1);
//                break;
//
//            case "avatar_m2":
//                holder.profile_image.setImageResource(R.drawable.avatar_m_2);
//                break;
//
//            case "avatar_m3":
//                holder.profile_image.setImageResource(R.drawable.avatar_m_3);
//                break;
//
//            default:
//                break;
        }

    }

    @Override
    public int getItemCount() {
        return musers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username= itemView.findViewById(R.id.username);
            profile_image= itemView.findViewById(R.id.profile_image);

        }
    }
}
