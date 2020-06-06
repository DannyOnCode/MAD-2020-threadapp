package com.threadteam.thread.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

public class ViewDividerViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public TextView serverDividing;
    public TextView serverLine;

    public ViewDividerViewHolder(@NonNull View itemView) {
        super(itemView);
        serverDividing = itemView.findViewById(R.id.serverDivider);
        serverLine = itemView.findViewById(R.id.serverDividerLine);

    }

}
