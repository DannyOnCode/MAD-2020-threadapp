package com.threadteam.thread.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

public class PostsItemViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    private ConstraintLayout BasePostItemConstraintLayout;
    private CardView PostItemCardView;
    private ConstraintLayout PostItemConstraintLayout;
    public ImageView PostImageView;
    public TextView PostTitleTextView;
    public TextView PostDescTextView;
    public TextView PostSenderTextView;
    public TextView PostTimestampTextView;

    public PostsItemViewHolder(@NonNull View itemView) {
        super(itemView);

        BasePostItemConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.basePostItemConstraintLayout);
        PostItemCardView = (CardView) itemView.findViewById(R.id.postItemCardView);
        PostItemConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.postItemConstraintLayout);
        PostImageView = (ImageView) itemView.findViewById(R.id.postImageView);
        PostTitleTextView = (TextView) itemView.findViewById(R.id.postTitleTextView);
        PostDescTextView = (TextView) itemView.findViewById(R.id.postDescTextView);
        PostSenderTextView = (TextView) itemView.findViewById(R.id.postSenderTextView);
        PostTimestampTextView = (TextView) itemView.findViewById(R.id.postTimestampTextView);
    }
}
