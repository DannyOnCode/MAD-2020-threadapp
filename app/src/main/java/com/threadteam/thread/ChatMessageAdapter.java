package com.threadteam.thread;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.models.ChatMessage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {

    //DATA STORE
    public List<ChatMessage> chatMessageList;

    public ChatMessageAdapter(List<ChatMessage> chatMessages) {
        this.chatMessageList = chatMessages;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ChatMessageViewHolder viewHolder;

        View item = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.activity_partial_chatmessage,
                parent,
                false
        );

        viewHolder = new ChatMessageViewHolder(item);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        holder.MessageTextView.setText(chatMessageList.get(position).get_message());
        holder.SenderTextView.setText(chatMessageList.get(position).get_sender());

        Timestamp ts = chatMessageList.get(position).get_timestamp();
        Date date = new Date(ts.getTime());
        String formattedString = new SimpleDateFormat("d/MM/yyyy h:mma").format(date);

        holder.TimestampTextView.setText(formattedString);
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }
}
