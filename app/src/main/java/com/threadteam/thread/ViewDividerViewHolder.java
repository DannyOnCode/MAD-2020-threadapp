package com.threadteam.thread;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ViewDividerViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public TextView serverDividing;
    public TextView serverLine;

    public ViewDividerViewHolder(@NonNull View itemView) {
        super(itemView);
        serverDividing = (TextView) itemView.findViewById(R.id.serverDivider);
        serverLine = (TextView) itemView.findViewById(R.id.serverDividerLine);

    }

}
