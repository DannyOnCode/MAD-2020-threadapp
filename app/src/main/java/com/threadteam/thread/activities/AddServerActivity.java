package com.threadteam.thread.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.models.Server;

public class AddServerActivity extends AppCompatActivity {

    // LOGGING
    private LogHandler logHandler = new LogHandler("ViewServers Activity");

    // FIREBASE
    //
    // currentUser:             CURRENT USER FOR THE CURRENT SESSION
    // firebaseAuth:            FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION
    // databaseRef:             FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION
    // subscriptionListener:    CHILD EVENT LISTENER FOR RETRIEVING ALL USER'S JOINED/OWNED SERVERS

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;

    //DATA STORE
    private String joinServerID;

    // VIEW OBJECTS
    //
    // BaseAddServerNSV:        HANDLES SCROLLING AND HIDING OF THE KEYBOARD ON TOUCH
    // JoinServerIdEditText:    ALLOWS USER TO ENTER THE SERVER ID OR SHARE CODE TO JOIN A SERVER.
    // JoinServerButton:        TRIGGERS JOIN SERVER LOGIC.
    // MakeServerNameEditText:  ALLOWS USER TO ENTER THE NAME FOR A NEW SERVER.
    // MakeServerDescEditText:  ALLOWS USER TO ENTER THE DESCRIPTION FOR A NEW SERVER.
    // JoinServerButton:        TRIGGERS CREATE SERVER LOGIC.
    // TopNavToolbar:           TOOLBAR OBJECT THAT HANDLES UPWARDS NAVIGATION AND THE TITLE

    private NestedScrollView BaseAddServerNSV;
    private EditText JoinServerIdEditText;
    private Button JoinServerButton;
    private EditText MakeServerNameEditText;
    private EditText MakeServerDescEditText;
    private Button MakeServerButton;
    private androidx.appcompat.widget.Toolbar TopNavToolbar;

    // ACTIVITY STATE MANAGEMENT METHODS

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        logHandler.printDefaultLog(LogHandler.STATE_ON_CREATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addserver);

        // BIND TOOLBARS
        View topNavView = findViewById(R.id.addServerNavbarInclude);
        TopNavToolbar = (Toolbar) topNavView.findViewById(R.id.topNavToolbar);

        logHandler.printDefaultLog(LogHandler.TOOLBAR_BOUND);

        // SETUP TOOLBARS
        this.setSupportActionBar(TopNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TopNavToolbar.setTitle("Add Server");

        logHandler.printDefaultLog(LogHandler.TOOLBAR_SETUP);

        // BIND VIEW OBJECTS
        BaseAddServerNSV = findViewById(R.id.baseAddServerNSV);
        JoinServerIdEditText = findViewById(R.id.joinServerIdEditText);
        JoinServerButton = findViewById(R.id.joinServerButton);
        MakeServerNameEditText = findViewById(R.id.makeServerNameEditText);
        MakeServerDescEditText = findViewById(R.id.makeServerDescEditText);
        MakeServerButton = findViewById(R.id.makeServerButton);

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);

        // SETUP VIEW OBJECTS
        BaseAddServerNSV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) AddServerActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view  = AddServerActivity.this.getCurrentFocus();
                if (view == null) {
                    view = new View(AddServerActivity.this);
                }
                if (imm != null) {
                    logHandler.printLogWithMessage("BaseAddServerNSV detected touch, hiding keyboard!");
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return false;
            }
        });

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

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_SETUP);

        //INITIALISE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null) {
            logHandler.printDefaultLog(LogHandler.FIREBASE_USER_NOT_FOUND);
            Intent backToLogin = new Intent(AddServerActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            return;
        }
        logHandler.printDefaultLog(LogHandler.FIREBASE_USER_FOUND);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        logHandler.printDefaultLog(LogHandler.FIREBASE_INITIALISED);
    }

    // CLASS METHODS

    private void handleJoinServer() {
        logHandler.printLogWithMessage("User tapped on Join Server; Attempting to join server...");

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
                    logHandler.printDatabaseResultLog(".getValue()", "Subscribed Server ID", "testUserNotSubscribed", (String) dataSnapshot.getValue());
                    displayError("User is already subscribed to this server!");
                } else {
                    logHandler.printDatabaseResultLog(".getValue()", "Subscribed Server ID", "testUserNotSubscribed", "null");
                    logHandler.printLogWithMessage("Subscribing user to server!");

                    // Subscribe user to server
                    databaseRef.child("users")
                               .child(userId)
                               .child("_subscribedServers")
                               .child(joinServerID)
                               .setValue(true);

                    // Add user to list of members
                    databaseRef.child("members")
                               .child(joinServerID)
                               .child(currentUser.getUid())
                               .setValue(true);

                    logHandler.printLogWithMessage("Server successfully joined; returning user back to ViewServers Activity!");
                    returnToViewServers();

                    finish();
                }
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
        } else {
            logHandler.printLogWithMessage("User entered a full ServerID!");

            databaseRef.child("servers")
                       .child(joinServerID)
                       .addListenerForSingleValueEvent(testServerExists);
        }
    }

    private void handleMakeServer() {
        logHandler.printLogWithMessage("User tapped on Create Server; Attempting to create server...");

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
        databaseRef.child("users").child(userId).child("_subscribedServers").child(newServerId).setValue(true);
        databaseRef.child("members").child(newServerId).child(currentUser.getUid()).setValue(true);

        logHandler.printLogWithMessage("New server details have been pushed to database; returning user back to ViewServers Activity!");
        returnToViewServers();

        finish();
    }

    private void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        logHandler.printLogWithMessage(message);
    }

    private void returnToViewServers() {
        Intent returnToViewServers = new Intent(AddServerActivity.this, ViewServersActivity.class);
        returnToViewServers.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(returnToViewServers, 0);
        logHandler.printActivityIntentLog("ViewServers Activity");
    }
}
