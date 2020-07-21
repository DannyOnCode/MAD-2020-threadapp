package com.threadteam.thread.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

public class ViewCommentDividerViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public TextView commentDividing;
    public TextView commentLine;

    public ViewCommentDividerViewHolder(@NonNull View itemView) {
        super(itemView);
        commentDividing = itemView.findViewById(R.id.commentDivider);
        commentLine = itemView.findViewById(R.id.commentDividerLine);
    }

}
