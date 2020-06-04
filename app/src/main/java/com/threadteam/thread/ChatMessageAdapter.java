package com.threadteam.thread;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //DATA STORE
    public List<ChatMessage> chatMessageList;
    public String currentUserUID;

    public ChatMessageAdapter(List<ChatMessage> chatMessages) {
        this.chatMessageList = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessageList.get(position).get_senderUID().equals(currentUserUID)) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) {
            View item = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.activity_partial_chatmessage_outgoing,
                    parent,
                    false
            );
            return new OutgoingChatMessageViewHolder(item);
        } else {
            View item = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.activity_partial_chatmessage_incoming,
                    parent,
                    false
            );
            return new IncomingChatMessageViewHolder(item);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Long tsMillis = chatMessageList.get(position).getTimestampMillis();

        String timeString = "Loading...";
        if(tsMillis != null) {
            Date date = new Date(tsMillis);
            timeString = new SimpleDateFormat("d/MM/yyyy h:mma").format(date);
        }

        if(getItemViewType(position) == 0) {
            ((OutgoingChatMessageViewHolder) holder).MessageTextView.setText(chatMessageList.get(position).get_message());
            ((OutgoingChatMessageViewHolder) holder).SenderTextView.setText(chatMessageList.get(position).get_sender());
            ((OutgoingChatMessageViewHolder) holder).TimestampTextView.setText(timeString);
        } else {
            ((IncomingChatMessageViewHolder) holder).MessageTextView.setText(chatMessageList.get(position).get_message());
            ((IncomingChatMessageViewHolder) holder).SenderTextView.setText(chatMessageList.get(position).get_sender());
            ((IncomingChatMessageViewHolder) holder).TimestampTextView.setText(timeString);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }
}
