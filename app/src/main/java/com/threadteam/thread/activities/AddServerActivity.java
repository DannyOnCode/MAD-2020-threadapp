package com.threadteam.thread.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.Utils;
import com.threadteam.thread.models.Server;

public class AddServerActivity extends _MainBaseActivity {

    //DATA STORE
    private String joinServerID;

    // VIEW OBJECTS
    //
    // JoinServerIdEditText:    ALLOWS USER TO ENTER THE SERVER ID OR SHARE CODE TO JOIN A SERVER.
    // JoinServerButton:        TRIGGERS JOIN SERVER LOGIC.
    // MakeServerNameEditText:  ALLOWS USER TO ENTER THE NAME FOR A NEW SERVER.
    // MakeServerDescEditText:  ALLOWS USER TO ENTER THE DESCRIPTION FOR A NEW SERVER.
    // JoinServerButton:        TRIGGERS CREATE SERVER LOGIC.
    // TopNavToolbar:           TOOLBAR OBJECT THAT HANDLES UPWARDS NAVIGATION AND THE TITLE

    private EditText JoinServerIdEditText;
    private Button JoinServerButton;
    private EditText MakeServerNameEditText;
    private EditText MakeServerDescEditText;
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
    int setLayoutIDForContentView() {
        return R.layout.activity_addserver;
    }

    @Override
    AppCompatActivity setCurrentActivity() {
        return AddServerActivity.this;
    }

    @Override
    String setTitleForActivity() {
        return "Add Server";
    }

    @Override
    ImageButton setMainActionButton() {
        return null;
    }

    @Override
    Toolbar setTopNavToolbar() {
        View topNavView = findViewById(R.id.addServerNavbarInclude);
        return (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
    }

    @Override
    ActionMenuView setBottomToolbarAMV() {
        return null;
    }

    @Override
    void DoAdditionalSetupForToolbars() {
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void BindViewObjects() {
        JoinServerIdEditText = findViewById(R.id.joinServerIdEditText);
        JoinServerButton = findViewById(R.id.joinServerButton);
        MakeServerNameEditText = findViewById(R.id.makeServerNameEditText);
        MakeServerDescEditText = findViewById(R.id.makeServerDescEditText);
        MakeServerButton = findViewById(R.id.makeServerButton);
    }

    @Override
    void SetupViewObjects() {
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
    void AttachListeners() { }

    @Override
    void DestroyListeners() { }

    @Override
    int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }

    // ACTIVITY SPECIFIC METHODS

    private void handleJoinServer() {
        logHandler.printLogWithMessage("User tapped on Join Server; Attempting to join server...");
        hideKeyboard();

        final String userId = currentUser.getUid();
        joinServerID = JoinServerIdEditText.getText().toString();

        // testUserNotSubscribed:   TESTS IF USER IS ALREADY SUBSCRIBED. IF THEY ARE NOT, SUBSCRIBE THE USER.
        //                          THIS IS THE END OF THE JOIN SERVER PIPELINE.
        //                          CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                              .child(userId)
        //                                                              .child("_subscribedServers")
        //                                                              .child(joinServerID)
        //                                                              .addListenerForSingleValueEvent(testUserNotSubscribed);
        //                          SHOULD NOT BE USED INDEPENDENTLY.

        final ValueEventListener testUserNotSubscribed = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    logHandler.printDatabaseResultLog(".getValue()", "Test Value", "testUserNotSubscribed", "not null");
                    displayError("User is already subscribed to this server!");
                    return;
                }
                logHandler.printDatabaseResultLog(".getValue()", "Test Value", "testUserNotSubscribed", "null");
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

                // User join message
                Utils.SendUserActionSystemMessage(logHandler, databaseRef, userId, " has joined the server!", joinServerID);

                logHandler.printLogWithMessage("Server successfully joined; returning user back to ViewServers Activity!");
                returnToViewServers();

                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
                displayError("Database Error! Action could not be completed. Maybe wait a while and try again?");
            }
        };

        // testServerExists:    TESTS IF SERVER EXISTS FOR A SERVER ID. IF IT DOES, PROCEED TO CHECK IF USER IS
        //                      SUBSCRIBED AND MOVE ON IN THE PIPELINE.
        //                      THIS CONTINUES THE JOIN SERVER PIPELINE.
        //                      CORRECT INVOCATION CODE:  databaseRef.child("servers")
        //                                                           .child(joinServerID)
        //                                                           .addListenerForSingleValueEvent(testServerExists);

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
                            .addListenerForSingleValueEvent(testUserNotSubscribed);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
                displayError("Database Error! Action could not be completed. Maybe wait a while and try again?");
            }
        };

        // shareCodeLookup:     CHECKS IF A 6-DIGIT ALPHANUMERIC ENTRY IS A SHARE CODE BY LOOKING UP THE ID. IF
        //                      IT IS A VALID SHARE CODE, RETRIEVE THE SERVER ID AND PASS IT TO testServerExists.
        //                      THIS IS THE START OF THE JOIN SERVER PIPELINE.
        //                      CORRECT INVOCATION CODE:  databaseRef.child("shares")
        //                                                           .child(joinServerID)
        //                                                           .addListenerForSingleValueEvent(shareCodeLookup);

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

    private void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        logHandler.printLogWithMessage(message);
    }

    private void returnToViewServers() {
        Intent returnToViewServers = new Intent(currentActivity, ViewServersActivity.class);
        returnToViewServers.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(returnToViewServers, 0);
        logHandler.printActivityIntentLog("ViewServers Activity");
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) currentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view  =currentActivity.getCurrentFocus();
        if (view == null) {
            view = new View(currentActivity);
        }
        if (imm != null) {
            logHandler.printLogWithMessage("Hiding keyboard!");
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
