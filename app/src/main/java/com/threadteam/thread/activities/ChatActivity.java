package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

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
import com.threadteam.thread.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

public class ChatActivity extends AppCompatActivity {

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
    private String shareCode;
    private String username;

    // VIEW OBJECTS
    //
    // ChatMessageRecyclerView: DISPLAYS ALL CHAT MESSAGES IN THE SERVER. USES adapter AS ITS ADAPTER.
    // MessageEditText:         CONTAINS TEXT DATA TO BE SENT UPON USER TAPPING SendMsgButton.
    // SendMsgButton:           TRIGGERS SENDING OF TEXT DATA TO THE SERVER
    // TopNavToolbar:           TOOLBAR OBJECT THAT HANDLES UPWARDS NAVIGATION AND THE TITLE


    private RecyclerView ChatMessageRecyclerView;
    private EditText MessageEditText;
    private Button SendMsgButton;
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

    private void resetShareCode() {
        logHandler.printLogWithMessage("Resetting Share Code (if not null)!");

        if(shareCode != null) {
            databaseRef.child("shares").child(shareCode).setValue(null);
            shareCode = null;
        }
    }

    private void returnToViewServers() {
        Intent returnToViewServers = new Intent(ChatActivity.this, ViewServersActivity.class);
        startActivity(returnToViewServers);
        logHandler.printActivityIntentLog("ViewServers Activity");

        onStop();
    }

    // TOOLBAR OVERRIDE METHODS

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, SHARE_SERVER_MENU_ITEM, Menu.NONE, "Share Server");
        menu.add(Menu.NONE, LEAVE_SERVER_MENU_ITEM, Menu.NONE, "Leave Server");
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == SHARE_SERVER_MENU_ITEM) {
            logHandler.printLogWithMessage("User tapped on Share Server Menu Item!");

            // LOGGING FOR SHARE SERVER POPUP WINDOW
            LogHandler popupLogHandler = new LogHandler("ShareServerPopupWindow");

            popupLogHandler.printLogWithMessage("Configuring Popup!");

            ConstraintLayout BaseChatConstraintLayout = findViewById(R.id.baseChatConstraintLayout);

            View popupView = LayoutInflater.from(this.getBaseContext()).inflate(
                    R.layout.activity_shareserver, BaseChatConstraintLayout, false
            );

            final PopupWindow shareCodePopup = new PopupWindow(
                    popupView,
                    (int) (BaseChatConstraintLayout.getWidth() * 0.8),
                    (int) (BaseChatConstraintLayout.getHeight() *0.8),
                    true);
            shareCodePopup.setTouchable(true);
            shareCodePopup.showAtLocation(BaseChatConstraintLayout, Gravity.CENTER, 0 ,0);
            shareCodePopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    resetShareCode();
                }
            });

            popupLogHandler.printLogWithMessage("Completed configuring Popup!");

            // BIND VIEW OBJECTS

            ConstraintLayout BaseShareCodeConstraintLayout = popupView
                    .findViewById(R.id.baseShareCodeConstraintLayout);
            Toolbar PopupToolbar = popupView.findViewById(R.id.shareCodeToolbar);
            final Button RefreshCodeButton = popupView.findViewById(R.id.refreshCodeButton);
            final TextView ShareCodeTextView = popupView.findViewById(R.id.shareCodeTextView);
            final TextView ShareCodeDescTextView = popupView.findViewById(R.id.shareCodeDescTextView);
            final TextView CodeExpiryTextView = popupView.findViewById(R.id.codeExpiryTextView);

            popupLogHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);

            // SETUP VIEW OBJECTS

            PopupToolbar.setTitle("Share Server Code");
            RefreshCodeButton.setText("GET CODE");
            ShareCodeTextView.setText("------");

            final CountDownTimer codeTimer = new CountDownTimer(10*60*1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long mins = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                    long secs = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - (mins * 60);
                    CodeExpiryTextView.setText(String.format(Locale.ENGLISH, "expires in %02d:%02d", mins, secs));
                }

                @Override
                public void onFinish() {
                    logHandler.printLogWithMessage("Timer has ended!");

                    RefreshCodeButton.setText("GET CODE");
                    ShareCodeTextView.setText("------");
                    ShareCodeDescTextView.setText(R.string.share_code_noCode);
                    CodeExpiryTextView.setText("expires in 00:00:00");

                    resetShareCode();
                }
            };

            popupLogHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_SETUP);

            // INITIALISE LISTENERS

            // sharesListener:  CHECKS THAT THERE IS NO IDENTICAL SHARE CODE IN SHARES AND CHANGES THE SHARE CODE IF
            //                  NECESSARY, TILL THERE IS NO CONFLICT IN THE DATABASE. AFTER THIS, PAIRS THE SHARE CODE
            //                  WITH THE CURRENT SERVER ID AND REFLECTS THE UPDATE GRAPHICALLY BY UPDATING ShareCodeTextView,
            //                  RefreshCodeButton, ShareCodeDescTextView AND STARTS codeTimer.
            //                  CORRECT INVOCATION CODE: databaseRef.child("shares")
            //                                                      .child(shareCode)
            //                                                      .addListenerForSingleValueEvent(sharesListener)
            //                  SHOULD NOT BE USED INDEPENDENTLY.

            final ValueEventListener sharesListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    while (dataSnapshot.getValue() != null) {
                        shareCode = Utils.GenerateAlphanumericID(6);
                    }

                    databaseRef.child("shares").child(shareCode).setValue(serverId);

                    ShareCodeTextView.setText(shareCode);
                    RefreshCodeButton.setText("REFRESH");
                    ShareCodeDescTextView.setText(R.string.share_code_hasCode);
                    codeTimer.start();

                    logHandler.printLogWithMessage("Code generated successfully, timer started!");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    logHandler.printDatabaseErrorLog(databaseError);
                }
            };

            popupLogHandler.printDefaultLog(LogHandler.FIREBASE_LISTENERS_INITIALISED);

            RefreshCodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // FOR REFRESH
                    resetShareCode();
                    codeTimer.cancel();
                    logHandler.printLogWithMessage("Timer has been cancelled (if not null)!");

                    // GET CODE
                    shareCode = Utils.GenerateAlphanumericID(6);
                    databaseRef.child("shares")
                               .child(shareCode)
                               .addListenerForSingleValueEvent(sharesListener);
                }
            });

            popupView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareCodePopup.dismiss();
                    codeTimer.cancel();
                }
            });

        } else if (item.getItemId() == LEAVE_SERVER_MENU_ITEM) {
            logHandler.printLogWithMessage("User tapped on Leave Server Menu Item!");

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Leave Server?");

            // deleteServer:    REMOVES ALL SUBSCRIPTIONS FOR A SINGLE SERVER ACROSS ALL ITS MEMBERS
            //                  INCLUDING THE OWNER, THEN DELETES ALL DATA PERTAINING TO IT AND SENDS
            //                  THE USER BACK TO THE ViewServers Activity
            //                  CORRECT INVOCATION CODE: databaseRef.child("members")
            //                                                      .child(serverId)
            //                                                      .addListenerForSingleValueEvent(deleteServer)
            //                  SHOULD NOT BE USED INDEPENDENTLY.

            final ValueEventListener deleteServer = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.getChildren()) {
                        if(data.getKey() == null) {
                            logHandler.printDatabaseResultLog(".getChildren().Key()", "Subscribed User ID", "deleteServer", "null");
                            continue;
                        }

                        String userID = data.getKey();
                        logHandler.printDatabaseResultLog(".getChildren().Key()", "Subscribed User ID", "deleteServer", userID);

                        databaseRef.child("users").child(userID).child("_subscribedServers").child(serverId).setValue(null);
                    }

                    // DELETE ALL SERVER DATA AFTER MEMBERS ARE GONE
                    databaseRef.child("servers").child(serverId).setValue(null);
                    databaseRef.child("messages").child(serverId).setValue(null);
                    databaseRef.child("members").child(serverId).setValue(null);

                    logHandler.printLogWithMessage("Server is completely deleted! Returning user back to View Server Activity!");
                    returnToViewServers();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    logHandler.printDatabaseErrorLog(databaseError);
                }
            };

            // presentDialog:   GETS OWNER ID AND COMPARES IT TO CURRENT USER'S ID, THEN ADAPTS THE ALERT DIALOG
            //                  ACCORDINGLY. FOLLOWING THIS, RUNS THE REST OF THE LEAVE SERVER LOGIC.
            //                  CORRECT INVOCATION CODE: databaseRef.child("servers")
            //                                                      .child(serverId)
            //                                                      .addListenerForSingleValueEvent(presentDialog)

            ValueEventListener presentDialog = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("_ownerID").getValue() == null) {
                        logHandler.printDatabaseResultLog(".child(\"_ownerID\").getValue()", "Server Owner ID", "getServerOwner", "null");
                        return;
                    }

                    String serverOwnerID = (String) dataSnapshot.child("_ownerID").getValue();

                    if(serverOwnerID == null) {
                        logHandler.printLogWithMessage("Could not cast value of dataSnapshot.child(\"_ownerID\").getValue() to String! Aborting Leave Server function!");
                        return;
                    }

                    logHandler.printDatabaseResultLog(".child(\"_ownerID\").getValue()", "Server Owner ID", "getServerOwner", serverOwnerID);

                    if(serverOwnerID.equals(currentUser.getUid())) {

                        // User is server owner
                        builder.setMessage("Are you sure you want to leave the server? As the owner, this will delete the server for everyone as well!");
                        builder.setNegativeButton("Yes, leave the server", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                logHandler.printLogWithMessage("Deleting server for all users!");
                                databaseRef.child("members")
                                           .child(serverId)
                                           .addListenerForSingleValueEvent(deleteServer);

                            }
                        });

                    } else {

                        // User is just a subscriber
                        builder.setMessage("Are you sure you want to leave the server? You'll have to re-enter another Server Share Code if you want to rejoin the server!");
                        builder.setNegativeButton("Yes, leave the server", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                logHandler.printLogWithMessage("Removing server subscription for user!");
                                databaseRef.child("users").child(currentUser.getUid()).child("_subscribedServers").child(serverId).setValue(null);
                                databaseRef.child("members").child(serverId).child(currentUser.getUid()).setValue(null);

                                logHandler.printLogWithMessage("Returning user back to View Server Activity!");
                                returnToViewServers();
                            }
                        });

                    }

                    builder.setNeutralButton("Cancel", null);
                    builder.create().show();
                    logHandler.printLogWithMessage("Presenting Leave Server Dialog!");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    logHandler.printDatabaseErrorLog(databaseError);
                }
            };

            databaseRef.child("servers")
                       .child(serverId)
                       .addListenerForSingleValueEvent(presentDialog);
        }

        return super.onOptionsItemSelected(item);
    }
}
