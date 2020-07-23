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
import com.threadteam.thread.viewholders.SystemChatMessageViewHolder;
import com.threadteam.thread.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Handles binding of chat messages to the appropriate view holders
 *
 * @author Eugene Long
 * @version 2.0
 * @since 1.0
 *
 * @see IncomingChatMessageViewHolder
 * @see OutgoingChatMessageViewHolder
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //DATA STORE

    /** Contains all chat message objects to be displayed. */
    public List<ChatMessage> chatMessageList;

    /** Contains a hash map mapping user ids to color ints. */
    public HashMap<String, Integer> userColorMap = new HashMap<>();

    /** Contains the current user's id. */
    public String currentUserUID;

    // CONSTRUCTOR

    public ChatMessageAdapter(List<ChatMessage> chatMessages) {
        this.chatMessageList = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessageList.get(position);
        if(message.get_senderID().equals(currentUserUID)) {
            return 0;
        } else if (message.get_senderID().equals("SYSTEM") && message.get_senderUsername().equals("SYSTEM")) {
            return 1;
        } else {
            return 2;
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
        } else if(viewType == 2) {
            View item = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.activity_partial_chatmessage_incoming,
                    parent,
                    false
            );
            return new IncomingChatMessageViewHolder(item);
        } else {
            View item = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.activity_partial_chatmessage_system,
                    parent,
                    false
            );
            return new SystemChatMessageViewHolder(item);
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
        } else if(getItemViewType(position) == 2) {
            ((IncomingChatMessageViewHolder) holder).SenderTextView.setText(senderUsername);
            ((IncomingChatMessageViewHolder) holder).MessageTextView.setText(message);
            ((IncomingChatMessageViewHolder) holder).TimestampTextView.setText(timeString);

            String senderId = chatMessageList.get(position).get_senderID();
            if(userColorMap.containsKey(senderId)) {
                ((IncomingChatMessageViewHolder) holder).SenderTextView.setTextColor(userColorMap.get(senderId));
            }

            LogHandler.staticPrintLog(
                    "Binding IncomingChatMessageViewHolder with data: " +
                            senderUsername + ", " + chatMessageList + ", " + timeString
            );
        } else {
            ((SystemChatMessageViewHolder) holder).MessageTextView.setText(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }
}
