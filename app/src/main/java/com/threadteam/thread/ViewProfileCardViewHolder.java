package com.threadteam.thread;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewProfileCardViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public ImageView userImage;
    public TextView userName;
    public TextView titleStatus;
    public TextView aboutMeDesc;

    public ViewProfileCardViewHolder(@NonNull View itemView) {
        super(itemView);

        userImage = itemView.findViewById(R.id.userProfilePictureEdit);
        userName = itemView.findViewById(R.id.userNameDisplay);
        titleStatus = itemView.findViewById(R.id.titleDisplay);
        aboutMeDesc = itemView.findViewById(R.id.aboutMeDesciption);
    }

}
