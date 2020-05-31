package com.threadteam.thread;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    private ConstraintLayout BaseChatItemConstraintLayout;
    private CardView ChatItemCardView;
    private ConstraintLayout ChatItemConstraintLayout;
    public TextView MessageTextView;
    public TextView SenderTextView;
    public TextView TimestampTextView;

    public ChatMessageViewHolder(@NonNull View itemView) {
        super(itemView);

        BaseChatItemConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.baseChatItemConstraintLayout);
        ChatItemCardView = (CardView) itemView.findViewById(R.id.chatItemCardView);
        ChatItemConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chatItemConstraintLayout);
        MessageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        SenderTextView = (TextView) itemView.findViewById(R.id.senderTextView);
        TimestampTextView = (TextView) itemView.findViewById(R.id.timestampTextView);
    }
}
