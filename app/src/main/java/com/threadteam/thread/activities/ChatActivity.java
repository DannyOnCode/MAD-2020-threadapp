package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.adapters.ChatMessageAdapter;
import com.threadteam.thread.R;
import com.threadteam.thread.models.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;

// CHAT ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// EUGENE LONG, S10193060J
//
// DESCRIPTION
// Displays all messages in the server chat
// Allows user to send messages
// Also allows the user to share/leave/delete the server
//
// NAVIGATION
// PARENT: VIEW SERVERS
// CHILDREN: N/A

public class ChatActivity extends ServerBaseActivity {

    // LOGGING
    private LogHandler logHandler = new LogHandler("Chat Activity");

    // FIREBASE
    //
    // currentUser:             CURRENT USER FOR THE CURRENT SESSION
    // firebaseAuth:            FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION
    // databaseRef:             FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION
    // chatListener:            CHILD EVENT LISTENER FOR RETRIEVING ALL CHAT MESSAGES IN CURRENT SERVER

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;
    private ChildEventListener chatListener;

    // DATA STORE
    //
    // serverId:                CONTAINS CURRENT SERVER ID DATA
    // adapter:                 ADAPTER FOR CHAT MESSAGE RECYCLER VIEW.
    //                          HANDLES STORAGE OF DISPLAYED CHAT MESSAGE DATA AS WELL.
    // scrollToLatestMessage:   TOGGLE FOR SCROLL TO BOTTOM UPON MESSAGE ADDED. DOES THIS ACTION IF TRUE.
    // SHARE_SERVER_MENU_ITEM:  CONSTANT DECLARING ID FOR THE SHARE SERVER MENU ITEM.
    // LEAVE_SERVER_MENU_ITEM:  CONSTANT DECLARING ID FOR THE LEAVE SERVER MENU ITEM.
    // shareCode:               CONTAINS THE CURRENT SHARING CODE OF THE SERVER (IF IT EXISTS)
    //                          ALSO WORKS AS A FLAG FOR THE resetShareCode FUNCTION.
    // username:                CONTAINS CURRENT USER'S USERNAME. USED TO SEND MESSAGES.

    private String serverId;
    private ChatMessageAdapter adapter;
    private Boolean scrollToLatestMessage = false;
    private Integer SHARE_SERVER_MENU_ITEM = -1;
    private Integer LEAVE_SERVER_MENU_ITEM = -2;
    private String username;

    // VIEW OBJECTS
    //
    // ChatMessageRecyclerView: DISPLAYS ALL CHAT MESSAGES IN THE SERVER. USES adapter AS ITS ADAPTER.
    // MessageEditText:         CONTAINS TEXT DATA TO BE SENT UPON USER TAPPING SendMsgButton.
    // SendMsgButton:           TRIGGERS SENDING OF TEXT DATA TO THE SERVER
    // TopNavToolbar:           TOOLBAR OBJECT THAT HANDLES UPWARDS NAVIGATION AND THE TITLE


    private RecyclerView ChatMessageRecyclerView;
    private EditText MessageEditText;
    private ImageButton SendMsgButton;
    private Toolbar TopNavToolbar;

    // ACTIVITY STATE MANAGEMENT METHODS

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logHandler.printDefaultLog(LogHandler.STATE_ON_CREATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // BIND TOOLBARS
        // NOTE:    IT IS IMPORTANT TO GET THE INCLUDE VIEWS BEFORE DOING FIND VIEW BY ID.
        //          THIS ENSURES THAT ANDROID CAN ALWAYS FIND THE CORRECT VIEW OBJECT.

        View includeView = findViewById(R.id.chatNavBarInclude);
        TopNavToolbar = (Toolbar) includeView.findViewById(R.id.topNavToolbar);

        logHandler.printDefaultLog(LogHandler.TOOLBAR_BOUND);

        // SETUP TOOLBARS
        TopNavToolbar.setTitle("Chat");
        this.setSupportActionBar(TopNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logHandler.printDefaultLog(LogHandler.TOOLBAR_SETUP);

        // BIND VIEW OBJECTS
        ChatMessageRecyclerView = findViewById(R.id.chatMessageRecyclerView);
        MessageEditText = findViewById(R.id.messageEditText);
        SendMsgButton = findViewById(R.id.sendMsgButton);

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);

        // SETUP VIEW OBJECTS
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
                    InputMethodManager imm = (InputMethodManager) ChatActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    View view  = ChatActivity.this.getCurrentFocus();
                    if (view == null) {
                        view = new View(ChatActivity.this);
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

        // Get serverId from Intent
        final Intent dataReceiver = getIntent();
        serverId = dataReceiver.getStringExtra("SERVER_ID");

        if (serverId == null) {
            logHandler.printGetExtrasResultLog("SERVER_ID", "null");
        }
        logHandler.printGetExtrasResultLog("SERVER_ID", serverId);

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_SETUP);

        // INITIALISE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null) {
            logHandler.printDefaultLog(LogHandler.FIREBASE_USER_NOT_FOUND);
            Intent backToLogin = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            return;
        }
        logHandler.printDefaultLog(LogHandler.FIREBASE_USER_FOUND);

        adapter.currentUserUID = currentUser.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        logHandler.printDefaultLog(LogHandler.FIREBASE_INITIALISED);

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

        databaseRef.child("users")
                   .child(currentUser.getUid())
                   .child("_username")
                   .addListenerForSingleValueEvent(getUsername);

        // chatListener:    HANDLES LOADING OF ALL CHAT MESSAGES, AS WELL AS UPDATING THE ADAPTER
        //                  ON MESSAGE ADDED/DELETED/CHANGED EVENTS
        //                  CORRECT INVOCATION CODE: databaseRef.child("messages")
        //                                                      .child(serverId)
        //                                                      .addChildEventListener(chatListener)
        //                  SHOULD BE CANCELLED UPON ACTIVITY STOP!

        chatListener = new ChildEventListener() {
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

        logHandler.printDefaultLog(LogHandler.FIREBASE_LISTENERS_INITIALISED);

        databaseRef.child("messages")
                   .child(serverId)
                   .addChildEventListener(chatListener);
    }

    @Override
    protected void onStop() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_STOP);

        // CANCEL CHILD EVENT LISTENERS ON ACTIVITY DESTROYED
        databaseRef.removeEventListener(chatListener);
        resetShareCode();

        super.onStop();
    }

    // CLASS METHODS

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
        } else {
            logHandler.printLogWithMessage("No message was pushed because there was no text after formatting!");
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == SHARE_SERVER_MENU_ITEM) {
            logHandler.printLogWithMessage("User tapped on Share Server Menu Item!");
            ConstraintLayout baseLayer = (ConstraintLayout) findViewById(R.id.baseChatConstraintLayout);
            showShareServerPopup(baseLayer, serverId);

        } else if (item.getItemId() == LEAVE_SERVER_MENU_ITEM) {
            logHandler.printLogWithMessage("User tapped on Leave Server Menu Item!");
            handleLeaveServerAlert(serverId, currentUser.getUid());

        } else if (item.getItemId() == android.R.id.home) {
            logHandler.printLogWithMessage("User tapped on Back Button!");

            Intent goToPosts = new Intent(ChatActivity.this, PostsActivity.class);
            String EXTRA_SERVER_ID_KEY = "SERVER_ID";
            String EXTRA_SERVER_ID_VALUE = serverId;
            goToPosts.putExtra(EXTRA_SERVER_ID_KEY, EXTRA_SERVER_ID_VALUE);
            goToPosts.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(goToPosts);
            logHandler.printActivityIntentLog("Post Activity");
            logHandler.printIntentExtrasLog(EXTRA_SERVER_ID_KEY, EXTRA_SERVER_ID_VALUE);
        }

        return super.onOptionsItemSelected(item);
    }
}
