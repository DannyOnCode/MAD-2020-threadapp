package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.widget.ActionMenuView;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.threadteam.thread.ChatMessageAdapter;
import com.threadteam.thread.R;
import com.threadteam.thread.models.ChatMessage;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    // DATA STORE
    private String username = "User01"; //TODO: Somehow get username and store it in here. For testing purposes, a default value has been used.
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private ChatMessageAdapter adapter;

    // VIEW OBJECTS
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

        // Setup Navbar and enable upwards navigation
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

        chatMessageList = loadMessagesFromServer(25, 0);

        // TEST CHAT MESSAGES

        adapter = new ChatMessageAdapter(chatMessageList, username);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ChatMessageRecyclerView.setLayoutManager(layoutManager);
        ChatMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ChatMessageRecyclerView.setAdapter(adapter);

        // No touch listener for now. (View only)

        // Load more messages (if possible) when top is reached
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                final LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if(llm.findLastVisibleItemPosition() == chatMessageList.size()-1) {
                    loadMoreMessages();
                }

            }
        };

        ChatMessageRecyclerView.addOnScrollListener(scrollListener);

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
                return true;
            }
        });
    }

    private void sendMessage() {
        String message = MessageEditText.getText().toString();

        // Stop newline spamming by doing some formatting
        String messageLines[] = message.split("\n");
        String formattedMessage = "";
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
            formattedMessage += trimmedLine + "\n";
        }

        // do a final trim
        formattedMessage = formattedMessage.trim();

        MessageEditText.setText(null);

        if(formattedMessage.length() > 0) {
            ChatMessage newMessage = new ChatMessage(username, formattedMessage, new Timestamp(System.currentTimeMillis()));
            adapter.chatMessageList.add(newMessage);
            adapter.notifyDataSetChanged();
        }

        // Scroll down to latest post
        ChatMessageRecyclerView.smoothScrollToPosition(adapter.chatMessageList.size()-1);
    }

    private List<ChatMessage> loadMessagesFromServer(Integer numMsg, Integer startIndex) {
        //TODO: get numMsg messages from the server starting from startIndex and return it in List<ChatMessage> format
        return new ArrayList<>();
    }

    private void loadMoreMessages() {
        //TODO: Load more data implementation
        //TODO: Check all messages for @ whispers and omit accordingly
    }

}
