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
import androidx.appcompat.widget.ActionMenuView;

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
import com.threadteam.thread.ChatMessageAdapter;
import com.threadteam.thread.R;
import com.threadteam.thread.models.ChatMessage;
import com.threadteam.thread.models.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChatActivity extends AppCompatActivity {

    private static final String LogTAG = "ThreadApp: ";

    // FIREBASE
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;
    private ChildEventListener chatListener;

    // DATA STORE
    private String serverId;
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private ChatMessageAdapter adapter;
    private Boolean scrollToLatestMessage = false;
    private Integer SHARE_SERVER_MENU_ITEM = -1;
    private Integer LEAVE_SERVER_MENU_ITEM = -2;
    private Integer queryLimit = 100;
    private String shareCode;

    // VIEW OBJECTS
    private String username;
    private RecyclerView ChatMessageRecyclerView;
    private EditText MessageEditText;
    private Button SendMsgButton;

    private Toolbar TopNavToolbar;
    private Toolbar BottomToolbar;
    private ActionMenuView BottomToolbarAMV;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // SETUP TOOLBARS
        View includeView = findViewById(R.id.chatNavBarInclude);
        TopNavToolbar = includeView.findViewById(R.id.topNavToolbar);

        TopNavToolbar.setTitle("Chat");
        this.setSupportActionBar(TopNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // BIND OBJECTS
        ChatMessageRecyclerView = findViewById(R.id.chatMessageRecyclerView);
        MessageEditText = findViewById(R.id.messageEditText);
        SendMsgButton = findViewById(R.id.sendMsgButton);

        SendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        adapter = new ChatMessageAdapter(chatMessageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ChatMessageRecyclerView.setLayoutManager(layoutManager);
        ((LinearLayoutManager) ChatMessageRecyclerView.getLayoutManager()).setStackFromEnd(true);
        ChatMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ChatMessageRecyclerView.setAdapter(adapter);

        // Use RecyclerView as scrim to dismiss keyboard
        ChatMessageRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) ChatActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view  = ChatActivity.this.getCurrentFocus();
                if (view == null) {
                    view = new View(ChatActivity.this);
                }
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

                if(dy < 0) {
                    scrollToLatestMessage = false;

                } else if(llm != null && llm.findLastCompletelyVisibleItemPosition() == adapter.chatMessageList.size()-1) {
                    Log.v(LogTAG, "Scrolled to bottom of chat!");
                    scrollToLatestMessage = true;
                }

            }
        };

        ChatMessageRecyclerView.addOnScrollListener(scrollListener);

        // INITIALISE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null) {
            Log.v(LogTAG, "User not signed in, returning to login activity!");
            Intent backToLogin = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            return;
        }
        adapter.currentUserUID = currentUser.getUid();

        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Get serverId from Intent
        final Intent dataReceiver = getIntent();
        serverId = dataReceiver.getStringExtra("SERVER_ID");

        // Use SingleValueEvent to get username
        ValueEventListener getUsername = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    Log.v(LogTAG, "Can't find username for user id!");
                    username = "anonymous";
                    return;
                }
                username = (String) dataSnapshot.getValue();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.v(LogTAG, "Database Error! " + databaseError.toString());
            }
        };
        databaseRef.child("users").child(currentUser.getUid()).child("_username").addListenerForSingleValueEvent(getUsername);

        // Declare main chatListener for onDataChange events
        chatListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey() == null) {
                    Log.v(LogTAG, "Message id is null! Aborting!");
                    return;
                }

                if(dataSnapshot.getValue() == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " value is null! Aborting!");
                    return;
                }

                String sender = (String) dataSnapshot.child("_sender").getValue();
                String senderUID = (String) dataSnapshot.child("_senderUID").getValue();
                String message = (String) dataSnapshot.child("_message").getValue();
                Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

                ChatMessage chatMessage;
                if (senderUID == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " senderUID is null! Aborting!");
                    return;
                } else if (sender == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " sender is null! Aborting!");
                    return;
                } else if (message == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " message is null! Aborting!");
                    return;
                } else if (timestampMillis == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " timestamp is null!");
                    chatMessage = new ChatMessage(senderUID, sender, message);
                } else {
                    chatMessage = new ChatMessage(senderUID, sender, message, timestampMillis);
                }

                chatMessage.set_id(dataSnapshot.getKey());
                adapter.chatMessageList.add(chatMessage);
                adapter.notifyItemInserted(chatMessageList.size());

                if(scrollToLatestMessage) {
                    Log.v(LogTAG, "Scrolling to latest message!");
                    ChatMessageRecyclerView.smoothScrollToPosition(Math.max(0, adapter.chatMessageList.size() - 1));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey() == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " value is null! Aborting!");
                    return;
                }

                Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

                if(timestampMillis == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " timestamp can't be created! Aborting!");
                    return;
                }

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
                if(dataSnapshot.getKey() == null) {
                    Log.v(LogTAG, "Message id is null! Aborting!");
                    return;
                }

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
                Log.v(LogTAG, "Database Error! " + databaseError.toString());
            }
        };

        databaseRef.child("messages").child(serverId).addChildEventListener(chatListener);
    }

    @Override
    protected void onStop() {
        databaseRef.removeEventListener(chatListener);
        resetShareCode();
        super.onStop();
    }

    private void sendMessage() {

        if(username == null) {
            Log.v(LogTAG, "Username is null! Aborting!");
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

        MessageEditText.setText(null);

        if(formattedMessage.length() > 0) {
            HashMap<String, Object> chatMessageHashMap = new HashMap<>();
            chatMessageHashMap.put("_senderUID", currentUser.getUid());
            chatMessageHashMap.put("_sender", username);
            chatMessageHashMap.put("_message", formattedMessage.toString());
            chatMessageHashMap.put("timestamp", System.currentTimeMillis());
            databaseRef.child("messages").child(serverId).push().setValue(chatMessageHashMap);
        }

        scrollToLatestMessage = true;
    }

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

            //BIND VIEW OBJECTS

            ConstraintLayout BaseShareCodeConstraintLayout = popupView
                    .findViewById(R.id.baseShareCodeConstraintLayout);
            Toolbar PopupToolbar = popupView.findViewById(R.id.shareCodeToolbar);
            final Button RefreshCodeButton = popupView.findViewById(R.id.refreshCodeButton);
            final TextView ShareCodeTextView = popupView.findViewById(R.id.shareCodeTextView);
            final TextView ShareCodeDescTextView = popupView.findViewById(R.id.shareCodeDescTextView);
            final TextView CodeExpiryTextView = popupView.findViewById(R.id.codeExpiryTextView);

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
                    RefreshCodeButton.setText("GET CODE");
                    ShareCodeTextView.setText("------");
                    ShareCodeDescTextView.setText(R.string.share_code_noCode);
                    CodeExpiryTextView.setText("expires in 00:00:00");

                    resetShareCode();
                }
            };

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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.v(LogTAG, "Database Error! " + databaseError.toString());
                }
            };

            RefreshCodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // FOR REFRESH
                    resetShareCode();
                    codeTimer.cancel();

                    // GET CODE
                    shareCode = Utils.GenerateAlphanumericID(6);
                    databaseRef.child("shares").child(shareCode).addListenerForSingleValueEvent(sharesListener);
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

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Leave Server?");

            ValueEventListener getServerOwner = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("_ownerID").getValue() == null) {
                        //TODO: Error
                        return;
                    }

                    if(dataSnapshot.child("_ownerID").getValue().equals(currentUser.getUid())) {

                        // User is server owner
                        builder.setMessage("Are you sure you want to leave the server? As the owner, this will delete the server for everyone as well!");
                        builder.setNegativeButton("Yes, leave the server", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final ValueEventListener getAllMembers = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot data : dataSnapshot.getChildren()) {
                                            if(data.getKey() == null) {
                                                Log.v(LogTAG, "Could not retrieve UserUID in members dir! Skipping!");
                                                continue;
                                            }

                                            String userUID = data.getKey();
                                            databaseRef.child("users").child(userUID).child("_subscribedServers").child(serverId).setValue(null);
                                        }

                                        // DELETE ALL SERVER DATA AFTER MEMBERS ARE GONE
                                        databaseRef.child("servers").child(serverId).setValue(null);
                                        databaseRef.child("messages").child(serverId).setValue(null);
                                        databaseRef.child("members").child(serverId).setValue(null);

                                        returnToViewServers();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                };
                                databaseRef.child("members").child(serverId).addListenerForSingleValueEvent(getAllMembers);
                            }
                        });

                    } else {

                        // User is just a subscriber
                        builder.setMessage("Are you sure you want to leave the server? You'll have to re-enter another Server Share Code if you want to rejoin the server!");
                        builder.setNegativeButton("Yes, leave the server", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseRef.child("users").child(currentUser.getUid()).child("_subscribedServers").child(serverId).setValue(null);
                                databaseRef.child("members").child(serverId).child(currentUser.getUid()).setValue(null);
                                returnToViewServers();
                            }
                        });

                    }

                    builder.setNeutralButton("Cancel", null);
                    builder.create().show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.v(LogTAG, "Database Error! " + databaseError.toString());
                }
            };
            databaseRef.child("servers").child(serverId).addListenerForSingleValueEvent(getServerOwner);
        }


        return super.onOptionsItemSelected(item);
    }

    private void resetShareCode() {
        if(shareCode != null) {
            databaseRef.child("shares").child(shareCode).setValue(null);
            shareCode = null;
        }
    }

    private void returnToViewServers() {
        Intent returnToViewServers = new Intent(ChatActivity.this, ViewServersActivity.class);
        startActivity(returnToViewServers);
        onStop();
    }
}
