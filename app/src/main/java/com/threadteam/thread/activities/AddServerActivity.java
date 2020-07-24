package com.threadteam.thread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.threadteam.thread.R;
import com.threadteam.thread.abstracts.MainBaseActivity;
import com.threadteam.thread.libraries.Utils;
import com.threadteam.thread.models.Server;

/**
 * This activity class handles the addition and creation of servers.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 1.0
 */

public class AddServerActivity extends MainBaseActivity {

    // DATA STORE

    /** Stores the identifier of the server that the user is trying to join. */
    private String joinServerID;

    // VIEW OBJECTS

    /** Allows the user to enter the server id or share code of the server they want to join */
    private EditText JoinServerIdEditText;

    /** When clicked, triggers the Join Server logic */
    private Button JoinServerButton;

    /** Allows the user to enter a name for a new server. */
    private EditText MakeServerNameEditText;

    /** Allows the user to enter a description for a new server. */
    private EditText MakeServerDescEditText;

    /** When clicked, triggers the Make Server logic */
    private Button MakeServerButton;

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

    // ABSTRACT OVERRIDE METHODS

    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_addserver;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return AddServerActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "Add Server";
    }

    @Override
    protected ImageButton setMainActionButton() {
        return null;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.addServerNavbarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return null;
    }

    @Override
    protected void DoAdditionalSetupForToolbars() {
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void BindViewObjects() {
        JoinServerIdEditText = findViewById(R.id.joinServerIdEditText);
        JoinServerButton = findViewById(R.id.joinServerButton);
        MakeServerNameEditText = findViewById(R.id.makeServerNameEditText);
        MakeServerDescEditText = findViewById(R.id.makeServerDescEditText);
        MakeServerButton = findViewById(R.id.makeServerButton);
    }

    @Override
    protected void SetupViewObjects() {
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

        MakeServerNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    MakeServerNameEditText.setText(MakeServerNameEditText.getText().toString().trim());
                }
            }
        });

        MakeServerDescEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    MakeServerDescEditText.setText(MakeServerDescEditText.getText().toString().trim());
                }
            }
        });
    }

    @Override
    protected void AttachOnStartListeners() { }

    @Override
    protected void DestroyOnStartListeners() { }

    @Override
    protected int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }

    // ACTIVITY SPECIFIC METHODS

    /**
     * This function tests if a user can join a server, and subscribes them to the server if possible.
     */

    private void handleJoinServer() {
        logHandler.printLogWithMessage("User tapped on Join Server; Attempting to join server...");
        hideKeyboard();

        final String userId = currentUser.getUid();
        joinServerID = JoinServerIdEditText.getText().toString();

        /*
         *  Subscribes the user if they have no existing subscription.
         *  Belongs to the Join Server Pipeline
         *  ShareCodeLookup -> TestServerExists -> [SubscribeUserToServer]
         *
         *  Database Path:      root/users/(userId)/_subscribedServers/(joinServerId)
         *  Usage:              Single ValueEventListener
         */

        final ValueEventListener subscribeUserToServer = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    logHandler.printDatabaseResultLog(".getValue()", "Test Value", "subscribeUserToServer", "not null");
                    displayError("User is already subscribed to this server!");
                    return;
                }
                logHandler.printDatabaseResultLog(".getValue()", "Test Value", "subscribeUserToServer", "null");
                logHandler.printLogWithMessage("Subscribing user to server!");

                // Subscribe user to server
                databaseRef.child("users")
                        .child(userId)
                        .child("_subscribedServers")
                        .child(joinServerID)
                        .setValue(0);

                // Add user to list of members
                databaseRef.child("members")
                        .child(joinServerID)
                        .child(currentUser.getUid())
                        .setValue(0);

                final String server = joinServerID;

                // User join message
                Utils.SendUserActionSystemMessage(logHandler, databaseRef, userId, " has joined the server!", joinServerID);
                logHandler.printLogWithMessage("Server successfully joined; returning user back to ViewServers Activity!");

                //User join Notification
                sendSystemNotification(joinServerID,userId," has joined the server!");
                logHandler.printLogWithMessage("Users in Server notified of new member!");

                //Server Chat Notifications for user
                FirebaseMessaging.getInstance().subscribeToTopic(server).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            logHandler.printLogWithMessage("SUBSCRIBED TO /topics/" + server +" SUCCESSFULLY");

                        }
                        else{
                            logHandler.printLogWithMessage("COULD NOT SUBSCRIBE");
                        }
                    }
                });


                //Server System Notifications for user
                FirebaseMessaging.getInstance().subscribeToTopic("system" + server).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            logHandler.printLogWithMessage("SUBSCRIBED TO /topics/system" +server +" SUCCESSFULLY");

                        }
                        else{
                            logHandler.printLogWithMessage("COULD NOT SUBSCRIBE");
                        }
                    }
                });

                //Server Posts Notifications for user
                FirebaseMessaging.getInstance().subscribeToTopic("posts" + server).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            logHandler.printLogWithMessage("SUBSCRIBED TO /topics/posts" +server +" SUCCESSFULLY");

                        }
                        else{
                            logHandler.printLogWithMessage("COULD NOT SUBSCRIBE");
                        }
                    }
                });

                returnToViewServers();

                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
                displayError("Database Error! Action could not be completed. Maybe wait a while and try again?");
            }
        };

        /*
         *  Tests if the server exists for JoinServerId. Moves on in the pipeline only if the server exists.
         *  Belongs to the Join Server Pipeline
         *  ShareCodeLookup -> ShareCodeLookup -> [TestServerExists] -> SubscribeUserToServer
         *
         *  Database Path:      root/servers/(joinServerID)
         *  Usage:              Single ValueEventListener
         */

        final ValueEventListener testServerExists = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    // Server does not exist
                    logHandler.printDatabaseResultLog(".getValue()", "Server Values", "testServerExists", "null");
                    displayError("No server exists for this ID!");
                } else {
                    logHandler.printDatabaseResultLog(".getValue()", "Server Values", "testServerExists", "not null");
                    // Test user subscription and move on in pipeline
                    databaseRef.child("users")
                            .child(userId)
                            .child("_subscribedServers")
                            .child(joinServerID)
                            .addListenerForSingleValueEvent(subscribeUserToServer);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
                displayError("Database Error! Action could not be completed. Maybe wait a while and try again?");
            }
        };

        /*
         *  Optional Pipeline. Checks if a 6-digit alphanumeric entry is a valid server share code.
         *  If valid, retrieves the server's id and calls the next stage in the pipeline.
         *  Belongs to the Join Server Pipeline
         *  ShareCodeLookup -> [ShareCodeLookup] -> TestServerExists -> SubscribeUserToServer
         *
         *  Database Path:      root/shares/(shareCode)
         *  Usage:              Single ValueEventListener
         */

        ValueEventListener shareCodeLookup = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    // Server Share Code does not exist
                    logHandler.printDatabaseResultLog(".getValue()", "Server ID From Share Code", "shareCodeLookup", "null");
                    displayError("Server Share Code is invalid! Check that your friend still has the Share Code page open!");
                } else {
                    // Continue using normal pipeline
                    joinServerID = (String) dataSnapshot.getValue();
                    logHandler.printDatabaseResultLog(".getValue()", "Server ID From Share Code", "shareCodeLookup", joinServerID);
                    databaseRef.child("servers")
                            .child(joinServerID)
                            .addListenerForSingleValueEvent(testServerExists);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
                displayError("Database Error! Action could not be completed. Maybe wait a while and try again?");
            }
        };

        //TestServerExists -> TestUserIsNotSubscribed -> SubscribeUserToServer
        if(joinServerID.length() == 6) {
            logHandler.printLogWithMessage("User entered a Server Share Code!");

            // Handle Server Share Code
            databaseRef.child("shares")
                    .child(joinServerID)
                    .addListenerForSingleValueEvent(shareCodeLookup);

        } else if (joinServerID.length() > 0) {
            logHandler.printLogWithMessage("User entered a ServerID!");

            databaseRef.child("servers")
                    .child(joinServerID)
                    .addListenerForSingleValueEvent(testServerExists);
        } else {
            JoinServerIdEditText.setError("Server ID can't be empty!");
        }
    }

    /**
     * This function validates the input from the user and attempts to make a server if successful.
     */

    private void handleMakeServer() {
        logHandler.printLogWithMessage("User tapped on Create Server; Attempting to create server...");
        hideKeyboard();


        String userId = currentUser.getUid();
        String makeServerName = MakeServerNameEditText.getText().toString().trim();
        String makeServerDesc = MakeServerDescEditText.getText().toString().trim();

        if(makeServerName.length() == 0) {
            MakeServerNameEditText.setError("Your server name can't be empty!");
            return;
        }

        Server newServer = new Server(userId, makeServerName, makeServerDesc);

        String newServerId = databaseRef.child("servers").push().getKey();

        if (newServerId == null) {
            logHandler.printLogWithMessage("Couldn't obtain serverId for new server! Aborting server creation!");
            return;
        }

        databaseRef.child("servers").child(newServerId).setValue(newServer);
        databaseRef.child("users").child(userId).child("_subscribedServers").child(newServerId).setValue(0);
        databaseRef.child("members").child(newServerId).child(currentUser.getUid()).setValue(0);

        logHandler.printLogWithMessage("New server details have been pushed to database; returning user back to ViewServers Activity!");
        returnToViewServers();

        finish();
    }

    /**
     * This function displays an error message to the user while logging it
     * @param message A string containing the error message
     */

    private void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        logHandler.printLogWithMessage(message);
    }

    /**
     * This function sends the user back to the View Servers Activity
     */

    private void returnToViewServers() {
        Intent returnToViewServers = new Intent(currentActivity, ViewServersActivity.class);
        returnToViewServers.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(returnToViewServers, 0);
        logHandler.printActivityIntentLog("ViewServers Activity");
    }

}
