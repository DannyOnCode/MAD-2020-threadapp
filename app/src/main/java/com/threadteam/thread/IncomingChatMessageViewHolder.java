package com.threadteam.thread;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class IncomingChatMessageViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public ConstraintLayout BaseChatItemConstraintLayout;
    public CardView ChatItemCardView;
    private ConstraintLayout ChatItemConstraintLayout;
    public TextView MessageTextView;
    public TextView SenderTextView;
    public TextView TimestampTextView;

    public IncomingChatMessageViewHolder(@NonNull View itemView) {
        super(itemView);

        BaseChatItemConstraintLayout = itemView.findViewById(R.id.baseChatItemInConstraintLayout);
        ChatItemCardView = itemView.findViewById(R.id.chatItemInCardView);
        ChatItemConstraintLayout = itemView.findViewById(R.id.chatItemInConstraintLayout);
        MessageTextView = itemView.findViewById(R.id.messageInTextView);
        SenderTextView = itemView.findViewById(R.id.senderInTextView);
        TimestampTextView = itemView.findViewById(R.id.timestampInTextView);
    }
}