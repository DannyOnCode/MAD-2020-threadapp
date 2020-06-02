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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.models.Server;

public class AddServerActivity extends AppCompatActivity {

    //FIREBASE
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

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
        final String joinServerId = JoinServerIdEditText.getText().toString();
        //TODO: look for server with id, if exists, add to user's subscribedServers and reload upon returning
        ValueEventListener getServerForID = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Server joinServer = dataSnapshot.child("servers").child(joinServerId).getValue(Server.class);
                if (joinServer != null) {
                    databaseRef.child("users").child("subscribedServers").push().setValue(joinServerId);
                } else {
                    //TODO: Error message
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: Error message
            }
        };

        databaseRef.addListenerForSingleValueEvent(getServerForID);

        setResult(RESULT_OK);
        finish();
    }

    private void handleMakeServer() {

        if (currentUser == null) {
            //TODO: Error message
            return;
        }

        String userId = currentUser.getUid();
        String makeServerName = MakeServerNameEditText.getText().toString();
        String makeServerDesc = MakeServerDescEditText.getText().toString();

        //TODO: Validation

        Server newServer = new Server(userId, makeServerName, makeServerDesc);

        String newServerId = databaseRef.child("servers").push().getKey();
        databaseRef.child("servers").child(newServerId).setValue(newServer);
        databaseRef.child("users").child(userId).child("subscribedServers").push().setValue(newServerId);

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
