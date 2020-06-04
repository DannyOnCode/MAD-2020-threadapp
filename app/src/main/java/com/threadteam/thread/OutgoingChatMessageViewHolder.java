package com.threadteam.thread;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class OutgoingChatMessageViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public ConstraintLayout BaseChatItemConstraintLayout;
    public CardView ChatItemCardView;
    private ConstraintLayout ChatItemConstraintLayout;
    public TextView MessageTextView;
    public TextView SenderTextView;
    public TextView TimestampTextView;

    public OutgoingChatMessageViewHolder(@NonNull View itemView) {
        super(itemView);

        BaseChatItemConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.baseChatItemOutConstraintLayout);
        ChatItemCardView = (CardView) itemView.findViewById(R.id.chatItemOutCardView);
        ChatItemConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chatItemOutConstraintLayout);
        MessageTextView = (TextView) itemView.findViewById(R.id.messageOutTextView);
        SenderTextView = (TextView) itemView.findViewById(R.id.senderOutTextView);
        TimestampTextView = (TextView) itemView.findViewById(R.id.timestampOutTextView);
    }
}
