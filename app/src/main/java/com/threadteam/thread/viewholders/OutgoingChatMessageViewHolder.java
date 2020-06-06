package com.threadteam.thread.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

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

        BaseChatItemConstraintLayout = itemView.findViewById(R.id.baseChatItemOutConstraintLayout);
        ChatItemCardView = itemView.findViewById(R.id.chatItemOutCardView);
        ChatItemConstraintLayout = itemView.findViewById(R.id.chatItemOutConstraintLayout);
        MessageTextView = itemView.findViewById(R.id.messageOutTextView);
        SenderTextView = itemView.findViewById(R.id.senderOutTextView);
        TimestampTextView = itemView.findViewById(R.id.timestampOutTextView);
    }
}
