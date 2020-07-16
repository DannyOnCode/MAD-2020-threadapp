package com.threadteam.thread.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

public class ViewCommentMessageViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public ConstraintLayout BaseCommentItemConstraintLayout;
    public CardView CommentItemCardView;
    public ConstraintLayout CommentItemConstraintLayout;
    public TextView CommentTextView;
    public TextView UserTextView;
    public TextView TitleTextView;
    public TextView LevelTextView;
    public TextView TimestampTextView;

    public ViewCommentMessageViewHolder(@NonNull View itemView) {
        super(itemView);

        BaseCommentItemConstraintLayout = itemView.findViewById(R.id.baseCommentConstraintLayout);
        CommentItemCardView = itemView.findViewById(R.id.commentItemCardView);
        CommentItemConstraintLayout = itemView.findViewById(R.id.commentItemConstraintLayout);
        CommentTextView = itemView.findViewById(R.id.commentMessage);
        UserTextView = itemView.findViewById(R.id.commentName);
        TitleTextView = itemView.findViewById(R.id.userTitle);
        LevelTextView = itemView.findViewById(R.id.levelTextView);
        TimestampTextView = itemView.findViewById(R.id.timestamp);
    }
}
