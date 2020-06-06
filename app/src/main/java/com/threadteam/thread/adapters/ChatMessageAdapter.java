package com.threadteam.thread.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.viewholders.IncomingChatMessageViewHolder;
import com.threadteam.thread.viewholders.OutgoingChatMessageViewHolder;
import com.threadteam.thread.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// CHAT MESSAGE ADAPTER CLASS
//
// PROGRAMMER-IN-CHARGE:
// EUGENE LONG, S10193060J
//
// DESCRIPTION
// ADAPTER USED BY ChatMessageRecyclerView in ChatActivity
// USES IncomingChatMessageViewHolder, OutgoingChatMessageViewHolder

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //DATA STORE
    public List<ChatMessage> chatMessageList;
    public String currentUserUID;

    // CONSTRUCTOR
    public ChatMessageAdapter(List<ChatMessage> chatMessages) {
        this.chatMessageList = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessageList.get(position).get_senderID().equals(currentUserUID)) {
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
            timeString = new SimpleDateFormat("d/MM/yyyy h:mma", Locale.UK).format(date);
        }

        String senderUsername = chatMessageList.get(position).get_senderUsername();
        String message = chatMessageList.get(position).get_message();

        if(getItemViewType(position) == 0) {
            ((OutgoingChatMessageViewHolder) holder).SenderTextView.setText(senderUsername);
            ((OutgoingChatMessageViewHolder) holder).MessageTextView.setText(message);
            ((OutgoingChatMessageViewHolder) holder).TimestampTextView.setText(timeString);

            LogHandler.staticPrintLog(
                    "Binding OutgoingChatMessageViewHolder with data: " +
                            senderUsername + ", " + message + ", " + timeString
            );
        } else {
            ((IncomingChatMessageViewHolder) holder).SenderTextView.setText(senderUsername);
            ((IncomingChatMessageViewHolder) holder).MessageTextView.setText(message);
            ((IncomingChatMessageViewHolder) holder).TimestampTextView.setText(timeString);

            LogHandler.staticPrintLog(
                    "Binding IncomingChatMessageViewHolder with data: " +
                            senderUsername + ", " + chatMessageList + ", " + timeString
            );
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }
}
