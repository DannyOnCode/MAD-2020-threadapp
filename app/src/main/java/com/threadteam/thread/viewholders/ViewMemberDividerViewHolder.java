package com.threadteam.thread.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

public class ViewMemberDividerViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public TextView memberDivider;
    public TextView memberDividerLine;

    public ViewMemberDividerViewHolder(@NonNull View itemView) {
        super(itemView);
        memberDivider = itemView.findViewById(R.id.memberDivider);
        memberDividerLine = itemView.findViewById(R.id.memberDividerLine);
    }

}
