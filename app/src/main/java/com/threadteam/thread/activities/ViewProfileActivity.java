package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.Utils;
import com.threadteam.thread.adapters.ViewProfileAdapter;
import com.threadteam.thread.models.User;

import java.util.ArrayList;
import java.util.List;

// VIEW PROFILE ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// DANNY CHAN, S10196363F
//
// DESCRIPTION
// Handles showing of the Profile of a user
// Handles the top and bottom custom toolbar implementation for this Activity
//
// NAVIGATION
// PARENT: NONE
// CHILDREN: EDIT PROFILE
// OTHER: VIEW SERVER

public class ViewProfileActivity extends AppCompatActivity {

    // LOGGING
    private LogHandler logHandler = new LogHandler("ViewProfile Activity");

    //DATA STORE
    //
    // LOG_OUT_MENU_ITEM_ID:    CONSTANT DECLARING ID FOR THE LOG OUT MENU ITEM.
    // mContext:                GET ACTIVITY CONTEXT.
    private final int LOG_OUT_MENU_ITEM_ID = -1;
    Context mContext;

    // FIREBASE
    //
    // currentUser:             CURRENT USER FOR THE CURRENT SESSION.
    // firebaseAuth:            FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION.
    // databaseRef:             FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION.
    // userDataListener:        VALUE EVENT LISTENER FOR RETRIEVING USER DATA.
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference ref;
    private ValueEventListener userDataListener;

    //VIEW OBJECT
    //
    // RecyclerView:            DISPLAYS PROFILE DETAILS AND JOINED SERVER. USES profileAdapter AS ADAPTER.
    // BottomToolbarAMV:        HANDLES THE MENU FOR THE BOTTOM TOOLBAR.
    // TopNavToolbar:           TOOLBAR OBJECT THAT HANDLES UPWARDS NAVIGATION AND THE TITLE.
    // BottomToolbarButton:     BOTTOM TOOLBAR MAIN ACTION BUTTON. USED TO HANDLE MAIN ACTIONS ON THIS
    //                          ACTIVITY. BOUND TO NAVIGATING TO ADD SERVER ACTIVITY IN THIS INSTANCE.
    private RecyclerView profileView;
    private Toolbar TopNavToolbar;
    private ActionMenuView BottomToolbarAMV;
    private ImageButton MainActionFAB;

    // ACTIVITY STATE MANAGEMENT METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);


        // BIND TOOLBARS
        // NOTE:    IT IS IMPORTANT TO GET THE INCLUDE VIEWS BEFORE DOING FIND VIEW BY ID.
        //          THIS ENSURES THAT ANDROID CAN ALWAYS FIND THE CORRECT VIEW OBJECT.
        View topNavView = findViewById(R.id.profileNavBarInclude);
        View bottomToolbarView = findViewById(R.id.profileBottomToolbarInclude);
        TopNavToolbar = topNavView.findViewById(R.id.topNavToolbar);
        BottomToolbarAMV = bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
        MainActionFAB = bottomToolbarView.findViewById(R.id.mainActionFAB);

        logHandler.printDefaultLog(LogHandler.TOOLBAR_BOUND);

        this.setSupportActionBar(TopNavToolbar);
        TopNavToolbar.setTitle("View Profile");
        BottomToolbarAMV.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_create_white_24);
        MainActionFAB.setImageDrawable(icon);
        MainActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToEditProfile = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
                startActivity(goToEditProfile);
                logHandler.printActivityIntentLog("Edit Profile Activity");
            }
        });

        logHandler.printDefaultLog(LogHandler.TOOLBAR_SETUP);

        // BIND VIEW OBJECTS
        profileView = findViewById(R.id.viewProfileRecyclerView);

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);

        // SETUP VIEW OBJECTS
        final ViewProfileAdapter profileAdapter = new ViewProfileAdapter(null);
        LinearLayoutManager pLayoutManager = new LinearLayoutManager(mContext);
        profileView.setLayoutManager(pLayoutManager);
        profileView.setAdapter(profileAdapter);
        profileView.setItemAnimator(new DefaultItemAnimator());

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_SETUP);

        // INITIALISE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            logHandler.printDefaultLog(LogHandler.FIREBASE_USER_NOT_FOUND);
            Intent backToLogin = new Intent(ViewProfileActivity.this, LoginActivity.class);
            logHandler.printActivityIntentLog("Login Activity");
            startActivity(backToLogin);
            return;
        }
        logHandler.printDefaultLog(LogHandler.FIREBASE_USER_FOUND);

        String userID = currentUser.getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("users").child(userID);


        // INITIALISE LISTENERS

        // userDataListener:    USED TO RETRIEVE USER DATA
        //                      CORRECT INVOCATION CODE: databaseRef.child("servers")
        //                                                          .child(serverId)
        //                                                          .addListenerForSingleValueEvent(addServerOnce)
        //                      SHOULD NOT BE USED INDEPENDENTLY.
        userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User();
                String userName = (String) dataSnapshot.child("_username").getValue();
                String profileImage = (String) dataSnapshot.child("_profileImageURL").getValue();
                String aboutMeMessages = (String) dataSnapshot.child("_aboutUsMessage").getValue();
                String statusDescription = (String) dataSnapshot.child("_statusMessage").getValue();

                logHandler.printDatabaseResultLog(".getValue()", "Current Profile Image", "userDataListener", profileImage);
                logHandler.printDatabaseResultLog(".getValue()", "Current Username", "userDataListener", userName);
                logHandler.printDatabaseResultLog(".getValue()", "Current Status/Title", "userDataListener", statusDescription);
                logHandler.printDatabaseResultLog(".getValue()", "Current Description", "userDataListener", aboutMeMessages);

                user.set_username(userName);
                user.set_profileImageURL(profileImage);
                user.set_aboutUsMessage(aboutMeMessages);
                user.set_statusMessage(statusDescription);

                List<String> serverList = new ArrayList<>();
                for(DataSnapshot data : dataSnapshot.child("_subscribedServers").getChildren()){
                    serverList.add(data.getKey());
                    logHandler.printDatabaseResultLog(".getKey()", "ServerID", "userDataListener", data.getKey());
                }

                user.set_subscribedServers(serverList);

                profileAdapter.userData = user;
                profileAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        logHandler.printDefaultLog(LogHandler.FIREBASE_LISTENERS_INITIALISED);

        ref.addValueEventListener(userDataListener);

    }

    @Override
    protected void onStop() {
        // CANCEL VALUE EVENT LISTENERS ON ACTIVITY STOP
        logHandler.printDefaultLog(LogHandler.STATE_ON_STOP);
        ref.removeEventListener(userDataListener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_DESTROY);
        Utils.ToggleMenuItemAlpha(
                this,
                R.id.viewProfileMenuItem,
                "View Profile",
                R.drawable.round_face_white_36,
                "round_face_white_36",
                true,
                logHandler
        );
        super.onDestroy();
    }

    //CLASS METHODS

    // TOOLBAR OVERRIDE METHODS
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Make ViewServers Button on menu bar look disabled
        Utils.ToggleMenuItemAlpha(
                this,
                R.id.viewProfileMenuItem,
                "View Profile",
                R.drawable.round_face_white_36,
                "round_face_white_36",
                false,
                logHandler
        );
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar_menu, BottomToolbarAMV.getMenu());
        TopNavToolbar.getMenu().add(Menu.NONE, LOG_OUT_MENU_ITEM_ID, Menu.NONE, "Log Out");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.viewServersMenuItem:

                Utils.StartActivityOnNewStack(
                        ViewProfileActivity.this,
                        ViewServersActivity.class,
                        "View Servers Activity",
                        null,
                        logHandler
                );

                // Reset disabled ActionMenuItemView button back to normal state
                Utils.ToggleMenuItemAlpha(
                        this,
                        R.id.viewProfileMenuItem,
                        "View Profile",
                        R.drawable.round_face_white_36,
                        "round_face_white_36",
                        true,
                        logHandler
                );

                ViewProfileActivity.this.finish();
                return true;

            case R.id.viewProfileMenuItem:
                // DISABLED
                return true;

            case LOG_OUT_MENU_ITEM_ID:
                firebaseAuth.signOut();
                Intent logOutToSignIn = new Intent(ViewProfileActivity.this, LoginActivity.class);
                startActivity(logOutToSignIn);
                logHandler.printActivityIntentLog("Login Activity");
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}