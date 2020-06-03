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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.widget.ActionMenuView;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.ChatMessageAdapter;
import com.threadteam.thread.R;
import com.threadteam.thread.models.ChatMessage;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        TopNavToolbar = (Toolbar) includeView.findViewById(R.id.topNavToolbar);

        TopNavToolbar.setTitle("Chat");
        this.setSupportActionBar(TopNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // BIND OBJECTS
        ChatMessageRecyclerView = (RecyclerView) findViewById(R.id.chatMessageRecyclerView);
        MessageEditText = (EditText) findViewById(R.id.messageEditText);
        SendMsgButton = (Button) findViewById(R.id.sendMsgButton);

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

                final LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

                if(llm == null) {
                    Log.v(LogTAG, "Linear Layout Manager for ChatMessageRecyclerView not found! Aborting onScroll Listener initialisation!");
                    return;
                }

                if(llm.findLastVisibleItemPosition() == chatMessageList.size()-1) {
                    //TODO: Implement loading more messages rather than all at once
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
                String message = (String) dataSnapshot.child("_message").getValue();
                Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

                //TODO: Implement message checking features here

                ChatMessage chatMessage;
                if (sender == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " sender is null! Aborting!");
                    return;
                } else if (message == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " message is null! Aborting!");
                    return;
                } else if (timestampMillis == null) {
                    Log.v(LogTAG, "Message " + dataSnapshot.getKey() + " timestamp is null!");
                    chatMessage = new ChatMessage(sender, message);
                } else {
                    chatMessage = new ChatMessage(sender, message, timestampMillis);
                }

                chatMessage.set_id(dataSnapshot.getKey());
                adapter.chatMessageList.add(chatMessage);
                adapter.notifyDataSetChanged();

                if(scrollToLatestMessage) {
                    Log.v(LogTAG, "Scrolling to latest message!");
                    ChatMessageRecyclerView.smoothScrollToPosition(Math.max(0, adapter.chatMessageList.size() - 1));
                    scrollToLatestMessage = false;
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == SHARE_SERVER_MENU_ITEM) {
            //TODO: Implement Share Server Id activity
        }
        return super.onOptionsItemSelected(item);
    }
}
