package com.threadteam.thread;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.models.Server;

public class ViewServerViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    private ConstraintLayout BaseViewServerConstraintLayout;
    private CardView ViewServerCardView;
    private ConstraintLayout ViewServerConstraintLayout;
    public TextView ViewServerNameTextView;
    public TextView ViewServerDescTextView;

    public ViewServerViewHolder(@NonNull View itemView) {
        super(itemView);

        BaseViewServerConstraintLayout = itemView.findViewById(R.id.baseViewServerConstraintLayout);
        ViewServerCardView = itemView.findViewById(R.id.viewServerCardView);
        ViewServerConstraintLayout = itemView.findViewById(R.id.viewServerConstraintLayout);
        ViewServerNameTextView = itemView.findViewById(R.id.viewServerNameTextView);
        ViewServerDescTextView = itemView.findViewById(R.id.viewServerDescTextView);
    }

}
