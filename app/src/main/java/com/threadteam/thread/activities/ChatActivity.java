package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.threadteam.thread.ChatMessageAdapter;
import com.threadteam.thread.R;
import com.threadteam.thread.models.ChatMessage;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    // DATA STORE
    private List<ChatMessage> chatMessageList;
    private ChatMessageAdapter adapter;

    // VIEW OBJECTS
    private Toolbar ChatNavbar;
    private RecyclerView ChatMessageRecyclerView;
    private EditText MessageEditText;
    private Button SendMsgButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup Navbar and enable upwards navigation
        ChatNavbar = (Toolbar) findViewById(R.id.chatNavbar);
        this.setSupportActionBar(ChatNavbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_chat);

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

        adapter = new ChatMessageAdapter(chatMessageList);
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
    }

    private void sendMessage() {
        //TODO: Send message after send message button is clicked
    }

    private List<ChatMessage> loadMessagesFromServer(Integer numMsg, Integer startIndex) {
        //TODO: get numMsg messages from the server starting from startIndex and return it in List<ChatMessage> format
        return null;
    }

    private void loadMoreMessages() {
        //TODO: Load more data implementation
    }
}
