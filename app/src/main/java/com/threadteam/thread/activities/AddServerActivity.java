package com.threadteam.thread.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.threadteam.thread.R;
import com.threadteam.thread.models.Server;

public class AddServerActivity extends AppCompatActivity {

    //VIEW OBJECTS
    private NestedScrollView BaseAddServerNSV;
    private EditText JoinServerIdEditText;
    private Button JoinServerButton;
    private EditText MakeServerNameEditText;
    private EditText MakeServerDescEditText;
    private Button MakeServerButton;

    private androidx.appcompat.widget.Toolbar TopNavToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addserver);

        // SETUP TOOLBARS
        View topNavView = findViewById(R.id.addServerNavbarInclude);
        TopNavToolbar = (Toolbar) topNavView.findViewById(R.id.topNavToolbar);

        this.setSupportActionBar(TopNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TopNavToolbar.setTitle("Add Server");

        // BIND VIEW OBJECTS
        BaseAddServerNSV = (NestedScrollView) findViewById(R.id.baseAddServerNSV);
        JoinServerIdEditText = (EditText) findViewById(R.id.joinServerIdEditText);
        JoinServerButton = (Button) findViewById(R.id.joinServerButton);
        MakeServerNameEditText = (EditText) findViewById(R.id.makeServerNameEditText);
        MakeServerDescEditText = (EditText) findViewById(R.id.makeServerDescEditText);
        MakeServerButton = (Button) findViewById(R.id.makeServerButton);

        JoinServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleJoinServer();
            }
        });

        MakeServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMakeServer();
            }
        });
    }

    private void handleJoinServer() {
        String joinServerId = JoinServerIdEditText.getText().toString();
        //TODO: look for server with id, if exists, add to user's subscribedServers and reload upon returning

        setResult(RESULT_OK);
        finish();
    }

    private void handleMakeServer() {
        int userId = 0; // TODO: Get user's id for make server
        String makeServerName = MakeServerNameEditText.getText().toString();
        String makeServerDesc = MakeServerDescEditText.getText().toString();
        Server newServer = new Server(userId, makeServerName, makeServerDesc);

        //TODO: upload server to database and add to user's subscribedServers

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
