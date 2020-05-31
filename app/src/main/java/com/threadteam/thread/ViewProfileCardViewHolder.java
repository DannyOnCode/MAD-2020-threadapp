package com.threadteam.thread;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

import org.w3c.dom.Text;

public class ViewProfileCardViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public ImageView userImage;
    public TextView userName;
    public TextView titleStatus;
    public ImageView editProfileBtn;
    public TextView aboutMeDesc;

    public ViewProfileCardViewHolder(@NonNull View itemView) {
        super(itemView);

        userImage = (ImageView) itemView.findViewById(R.id.userProfilePicture);
        userName = (TextView) itemView.findViewById(R.id.userNameDisplay);
        titleStatus = (TextView) itemView.findViewById(R.id.titleDisplay);
        editProfileBtn = (ImageView) itemView.findViewById(R.id.editProfileButton);
        aboutMeDesc = (TextView) itemView.findViewById(R.id.aboutMeDesciption);
    }

}
