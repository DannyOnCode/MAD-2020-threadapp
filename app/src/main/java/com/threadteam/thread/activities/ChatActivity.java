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
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.adapters.ChatMessageAdapter;
import com.threadteam.thread.models.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends ServerBaseActivityTemp {

    // DATA STORE
    //
    // adapter:                 ADAPTER FOR CHAT MESSAGE RECYCLER VIEW.
    //                          HANDLES STORAGE OF DISPLAYED CHAT MESSAGE DATA AS WELL.
    // scrollToLatestMessage:   TOGGLE FOR SCROLL TO BOTTOM UPON MESSAGE ADDED. DOES THIS ACTION IF TRUE.
    // username:                CONTAINS CURRENT USER'S USERNAME. USED TO SEND MESSAGES.

    private ChatMessageAdapter adapter;
    private Boolean scrollToLatestMessage = false;
    private String username;

    // VIEW OBJECTS
    //
    // ChatMessageRecyclerView: DISPLAYS ALL CHAT MESSAGES IN THE SERVER. USES adapter AS ITS ADAPTER.
    // MessageEditText:         CONTAINS TEXT DATA TO BE SENT UPON USER TAPPING SendMsgButton.
    // SendMsgButton:           TRIGGERS SENDING OF TEXT DATA TO THE SERVER

    private RecyclerView ChatMessageRecyclerView;
    private EditText MessageEditText;
    private ImageButton SendMsgButton;

    // INITIALISE LISTENERS

    // getUsername:     RETRIEVES CURRENT USER'S USERNAME
    //                  CORRECT INVOCATION CODE: databaseRef.child("users")
    //                                                      .child(currentUser.getUid())
    //                                                      .child("_username")
    //                                                      .addListenerForSingleValueEvent(getUsername)
    //                  SHOULD NOT BE USED INDEPENDENTLY.

    ValueEventListener getUsername = new ValueEventListener() {
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

    // chatListener:    HANDLES LOADING OF ALL CHAT MESSAGES, AS WELL AS UPDATING THE ADAPTER
    //                  ON MESSAGE ADDED/DELETED/CHANGED EVENTS
    //                  CORRECT INVOCATION CODE: databaseRef.child("messages")
    //                                                      .child(serverId)
    //                                                      .addChildEventListener(chatListener)
    //                  SHOULD BE CANCELLED UPON ACTIVITY STOP!

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

            ChatMessage chatMessage;

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
    void SetContentView() {
        setContentView(R.layout.activity_chat);
    }

    @Override
    AppCompatActivity setCurrentActivity() {
        return ChatActivity.this;
    }

    @Override
    String setTitleForActivity() {
        return "Chat";
    }

    @Override
    ImageButton setMainActionButton() {
        return null;
    }

    @Override
    Toolbar setTopNavToolbar() {
        View includeView = findViewById(R.id.chatNavBarInclude);
        return (Toolbar) includeView.findViewById(R.id.topNavToolbar);
    }

    @Override
    ActionMenuView setBottomToolbarAMV() {
        return null;
    }

    @Override
    void BindViewObjects() {
        ChatMessageRecyclerView = findViewById(R.id.chatMessageRecyclerView);
        MessageEditText = findViewById(R.id.messageEditText);
        SendMsgButton = findViewById(R.id.sendMsgButton);
    }

    @Override
    ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseChatConstraintLayout);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    void SetupViewObjects() {
        SendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
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
    void DoAdditionalSetupForFirebase() {
        adapter.currentUserUID = currentUser.getUid();
    }

    @Override
    void AttachListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .child("_username")
                .addListenerForSingleValueEvent(getUsername);

        databaseRef.child("messages")
                .child(serverId)
                .addChildEventListener(chatListener);
    }

    @Override
    void DestroyListeners() {
        if(chatListener != null) {
            databaseRef.removeEventListener(chatListener);
        }
    }

    @Override
    int setCurrentMenuItemID() {
        return R.id.chatMenuItem;
    }

    // ACTIVITY SPECIFIC METHODS

    private void sendMessage() {

        if(username == null) {
            logHandler.printLogWithMessage("Username is null (which it shouldn't be)! Aborting sendMessage()!");
            return;
        }

        String message = MessageEditText.getText().toString();

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
        logHandler.printLogWithMessage("User submitted message: " + message + " and was it was formatted as: " + formattedMessage.toString());

        MessageEditText.setText(null);

        if(formattedMessage.length() > 0) {
            HashMap<String, Object> chatMessageHashMap = new HashMap<>();
            chatMessageHashMap.put("_senderUID", currentUser.getUid());
            chatMessageHashMap.put("_sender", username);
            chatMessageHashMap.put("_message", formattedMessage.toString());
            chatMessageHashMap.put("timestamp", System.currentTimeMillis());
            databaseRef.child("messages").child(serverId).push().setValue(chatMessageHashMap);

            scrollToLatestMessage = true;
            logHandler.printLogWithMessage("Message pushed! Setting scrollToLatestMessage = true!");

            addExpForServerMember(currentUser.getUid(), serverId, 1, 60);
        } else {
            logHandler.printLogWithMessage("No message was pushed because there was no text after formatting!");
        }

    }
}
