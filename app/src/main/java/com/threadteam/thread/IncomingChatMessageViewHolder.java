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

        BaseChatItemConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.baseChatItemInConstraintLayout);
        ChatItemCardView = (CardView) itemView.findViewById(R.id.chatItemInCardView);
        ChatItemConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chatItemInConstraintLayout);
        MessageTextView = (TextView) itemView.findViewById(R.id.messageInTextView);
        SenderTextView = (TextView) itemView.findViewById(R.id.senderInTextView);
        TimestampTextView = (TextView) itemView.findViewById(R.id.timestampInTextView);
    }
}