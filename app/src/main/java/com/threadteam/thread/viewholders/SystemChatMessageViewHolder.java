package com.threadteam.thread.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

public class SystemChatMessageViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public TextView MessageTextView;

    public SystemChatMessageViewHolder(@NonNull View itemView) {
        super(itemView);

        MessageTextView = (TextView) itemView.findViewById(R.id.messageServerTextView);
    }
}
