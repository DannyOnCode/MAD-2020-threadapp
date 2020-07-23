package com.threadteam.thread.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.abstracts.ServerBaseActivity;
import com.threadteam.thread.libraries.Progression;
import com.threadteam.thread.R;
import com.threadteam.thread.adapters.ChatMessageAdapter;
import com.threadteam.thread.models.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This activity class handles displaying and interacting with the server's main chat.
 *
 * @author Main: Eugene Long
 * @author Notfications: Thabith
 * @version 2.0
 * @since 1.0
 */

public class ChatActivity extends ServerBaseActivity {

    // DATA STORE

    /** Adapter object for ChatMessageRecyclerView. */
    private ChatMessageAdapter adapter;

    /** Flag for scrolling to the latest message upon a new message being sent. */
    private Boolean scrollToLatestMessage = false;

    /** Stores the current user's username. Used when sending messages. */
    private String username;

    // VIEW OBJECTS

    /**
     * Handles the display of all chat messages in the server.
     * Uses ChatMessageAdapter as its adapter.
     * @see ChatMessageAdapter
     */
    private RecyclerView ChatMessageRecyclerView;

    /** Allows the user to key in a message to be sent in the chat. */
    private EditText MessageEditText;

    /** Triggers the send message logic. */
    private ImageButton SendMsgButton;

    // INITIALISE LISTENERS

    /**
     *  Retrieves the username of the current user.
     *
     *  Database Path:      root/users/(currentUser.getUid())/_username
     *  Usage:              Single ValueEventListener
     */

    private ValueEventListener getUsername = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Current Username", "getUsername", "null");
                username = "anonymous";
                return;
            }
            username = (String) dataSnapshot.getValue();
            logHandler.printDatabaseResultLog(".getValue()", "Current Username", "getUsername", username);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    /**
     *  Maps all server members to their respective titles based on their current exp.
     *
     *  Database Path:      root/members/(serverId)
     *  Usage:              ValueEventListener
     */

    private ValueEventListener mapUsersToTitle = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Member Data", "mapUsersToTitle", "null");
                return;
            }

            HashMap<String, Integer> colorMap = new HashMap<>();

            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (snapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog("snapshot.getKey()", "Member ID", "mapUsersToTitle", "null");
                    return;
                }

                String memberId = snapshot.getKey();
                logHandler.printDatabaseResultLog("snapshot.getKey()", "Member ID", "mapUsersToTitle", memberId);

                if (snapshot.getValue() == null) {
                    logHandler.printDatabaseResultLog("snapshot.getValue()", "Member EXP", "mapUsersToTitle", "null");
                    return;
                }

                int memberExp = ((Long) snapshot.getValue()).intValue();
                logHandler.printDatabaseResultLog("snapshot.getValue()", "Member EXP", "mapUsersToTitle", Integer.toString(memberExp));

                int memberLevel = Progression.ConvertExpToLevel(memberExp);
                int memberStage = Progression.ConvertLevelToStage(memberLevel);
                int memberColor = Progression.GetDefaultColorIntForStage(memberStage);

                colorMap.put(memberId, memberColor);
            }

            adapter.userColorMap = colorMap;
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    /**
     *  Handles the loading of chat messages into the adapter on message added/deleted/changed events.
     *
     *  Database Path:      root/messages/(serverId)
     *  Usage:              ChildEventListener
     */

    private ChildEventListener chatListener = new ChildEventListener() {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            logHandler.printLogWithMessage("Chat message added/loaded!");

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Message ID", "chatListener", "null");
                return;
            }

            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Message Values", "chatListener", "null");
                return;
            }

            String senderUID = (String) dataSnapshot.child("_senderUID").getValue();
            String sender = (String) dataSnapshot.child("_sender").getValue();
            String message = (String) dataSnapshot.child("_message").getValue();
            Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

            final ChatMessage chatMessage;

            if (senderUID == null) {
                logHandler.printDatabaseResultLog(".child(\"_senderUID\").getValue()", "Sender ID", "chatListener", "null");
                return;
            } else if (sender == null) {
                logHandler.printDatabaseResultLog(".child(\"_sender\").getValue()", "Sender Name", "chatListener", "null");
                return;
            } else if (message == null) {
                logHandler.printDatabaseResultLog(".child(\"_message\").getValue()", "Message", "chatListener", "null");
                return;
            } else if (timestampMillis == null) {
                logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "chatListener", "null");
                chatMessage = new ChatMessage(senderUID, sender, message);
            } else {
                chatMessage = new ChatMessage(senderUID, sender, message, timestampMillis);
            }

            chatMessage.set_id(dataSnapshot.getKey());

            logHandler.printDatabaseResultLog("", "Chat Message", "chatListener", chatMessage.toString());

            adapter.chatMessageList.add(chatMessage);
            adapter.notifyItemInserted(adapter.chatMessageList.size());

            if(scrollToLatestMessage) {
                logHandler.printLogWithMessage("scrollToLatestMessage = true; scrolling to latest message now!");
                ChatMessageRecyclerView.smoothScrollToPosition(Math.max(0, adapter.chatMessageList.size() - 1));
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            logHandler.printLogWithMessage("Chat Message changed! Updating timestamp!");

            // Note: Future features may introduce editing of messages, but for now it's just for updating the timestamp
            //       when Firebase Cloud Functions gives us the server side timestamp.

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Message ID", "chatListener", "null");
                return;
            }
            logHandler.printDatabaseResultLog(".getKey()", "Message ID", "chatListener", dataSnapshot.getKey());

            Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

            if(timestampMillis == null) {
                logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "chatListener", "null");
                return;
            }
            logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "chatListener", timestampMillis.toString());

            String messageId = dataSnapshot.getKey();

            for(int i=0; i<adapter.chatMessageList.size(); i++) {
                if(adapter.chatMessageList.get(i).get_id() != null && adapter.chatMessageList.get(i).get_id().equals(messageId)) {
                    adapter.chatMessageList.get(i).setTimestampMillis(timestampMillis);
                    adapter.notifyItemChanged(i);
                    return;
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            logHandler.printLogWithMessage("Chat messaged removed! Deleting chat message!");

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Message ID", "chatListener", "null");
                return;
            }
            logHandler.printDatabaseResultLog(".getKey()", "Message ID", "chatListener", dataSnapshot.getKey());

            for(int i=0; i<adapter.chatMessageList.size(); i++) {
                if(adapter.chatMessageList.get(i).get_id() != null &&
                        adapter.chatMessageList.get(i).get_id().equals(dataSnapshot.getKey())) {
                    adapter.chatMessageList.remove(i);
                    adapter.notifyItemRemoved(i);
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    // DEFAULT SUPER METHODS

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     * This implementation overrides the back button to send the user back to the Posts activity instead of View Servers.
     */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            logHandler.printLogWithMessage("User tapped on Back Button!");

            Intent goToPosts = new Intent(currentActivity, PostsActivity.class);
            PutExtrasForServerIntent(goToPosts);
            currentActivity.startActivity(goToPosts);
            logHandler.printActivityIntentLog("Posts");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ABSTRACT OVERRIDE METHODS

    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_chat;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return ChatActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "Chat";
    }

    @Override
    protected ImageButton setMainActionButton() {
        return null;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.chatNavBarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return null;
    }

    @Override
    protected void BindViewObjects() {
        ChatMessageRecyclerView = findViewById(R.id.chatMessageRecyclerView);
        MessageEditText = findViewById(R.id.messageEditText);
        SendMsgButton = findViewById(R.id.sendMsgButton);
    }

    @Override
    protected ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseChatConstraintLayout);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void SetupViewObjects() {
        SendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = MessageEditText.getText().toString();
                sendMessage(message);
            }
        });

        adapter = new ChatMessageAdapter(new ArrayList<ChatMessage>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ChatMessageRecyclerView.setLayoutManager(layoutManager);
        ((LinearLayoutManager) ChatMessageRecyclerView.getLayoutManager()).setStackFromEnd(true);
        ChatMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ChatMessageRecyclerView.setAdapter(adapter);

        // Use RecyclerView as scrim to dismiss keyboard
        ChatMessageRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    InputMethodManager imm = (InputMethodManager) currentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    View view  = currentActivity.getCurrentFocus();
                    if (view == null) {
                        view = new View(currentActivity);
                    }
                    if (imm != null && imm.isAcceptingText()) {
                        logHandler.printLogWithMessage("RecyclerView detected touch, hiding keyboard (if active)!");
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        // Load more messages (if possible) when top is reached.
        final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

                if(dy < 0 && scrollToLatestMessage) {
                    logHandler.printLogWithMessage("Scrolled up, toggled scrollToLatestMessage = false!");
                    scrollToLatestMessage = false;

                } else if(llm != null && llm.findLastCompletelyVisibleItemPosition() == adapter.chatMessageList.size()-1) {
                    logHandler.printLogWithMessage("Scrolled to bottom of chat, setting scrollToLatestMessage = true!");
                    scrollToLatestMessage = true;
                }

            }
        };

        ChatMessageRecyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    protected void DoAdditionalSetupForFirebase() {
        adapter.currentUserUID = currentUser.getUid();
    }

    @Override
    protected void AttachListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .child("_username")
                .addListenerForSingleValueEvent(getUsername);

        databaseRef.child("members")
                   .child(serverId)
                   .addValueEventListener(mapUsersToTitle);

        databaseRef.child("messages")
                   .child(serverId)
                   .addChildEventListener(chatListener);
    }

    @Override
    protected void DestroyListeners() {
        if(chatListener != null) {
            databaseRef.child("messages")
                       .child(serverId)
                       .removeEventListener(chatListener);
        }
        if(mapUsersToTitle != null) {
            databaseRef.child("members")
                       .child(serverId)
                       .removeEventListener(mapUsersToTitle);
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }

    // ACTIVITY SPECIFIC METHODS

    /**
     * Formats, checks and sends a message in the chat.
     * @param message The message to send in the chat
     */

    private void sendMessage(String message) {

        if(username == null) {
            logHandler.printLogWithMessage("Username is null (which it shouldn't be)! Aborting sendMessage()!");
            return;
        }

        String formattedMessage = formatMessage(message);
        logHandler.printLogWithMessage("User submitted message: " + message + " and was it was formatted as: " + formattedMessage);

        MessageEditText.setText(null);

        if(checkMessageIsValid(message)) {
            HashMap<String, Object> chatMessageHashMap = new HashMap<>();
            chatMessageHashMap.put("_senderUID", currentUser.getUid());
            chatMessageHashMap.put("_sender", username);
            chatMessageHashMap.put("_message", formattedMessage);
            chatMessageHashMap.put("timestamp", System.currentTimeMillis());
            databaseRef.child("messages").child(serverId).push().setValue(chatMessageHashMap);

            scrollToLatestMessage = true;
            logHandler.printLogWithMessage("Message sent! Setting scrollToLatestMessage = true!");

            AddExpForServerMember(currentUser.getUid(), serverId, 1, 60);

            sendNotification(serverId, currentUser.getUid(), ": " + formattedMessage);
            logHandler.printLogWithMessage("Notification sent to users in group! ");

        } else {
            logHandler.printLogWithMessage("Message was not sent because it was invalid!");
        }
    }

    /**
     * Formats a message to prepare it for sending to chat
     * @param message The message to format
     * @return The formatted message
     */

    private String formatMessage(String message) {
        // Stop newline spamming by doing some formatting
        String[] messageLines = message.split("\n");
        StringBuilder formattedMessage = new StringBuilder();
        int previousNewlines = 0;
        for(String line: messageLines) {
            String trimmedLine = line.trim();
            if(previousNewlines > 2 && trimmedLine.length() == 0) {
                continue;
            } else if(trimmedLine.length() == 0) {
                previousNewlines += 1;
            } else {
                previousNewlines = 0;
            }
            formattedMessage.append(trimmedLine).append("\n");
        }

        // do a final trim
        formattedMessage = new StringBuilder(formattedMessage.toString().trim());
        return formattedMessage.toString();
    }

    /**
     * Checks if a message is valid for sending to chat
     * @param message The message to check
     * @return A boolean representing the validity of the message
     */

    private Boolean checkMessageIsValid(String message) {
        return message.length() > 0;
    }
}
