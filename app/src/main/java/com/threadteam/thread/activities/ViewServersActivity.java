package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.RecyclerTouchListener;
import com.threadteam.thread.Utils;
import com.threadteam.thread.adapters.ViewServerAdapter;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.models.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// VIEW SERVERS ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// EUGENE LONG, S10193060J
//
// DESCRIPTION
// Handles showing of the servers a user is subscribed to
// Handles the top and bottom custom toolbar implementation for this Activity
//
// NAVIGATION
// PARENT: NONE
// CHILDREN: ADD SERVER / SERVER CHAT
// OTHER: VIEW PROFILE

public class ViewServersActivity extends AppCompatActivity {

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
    private ChildEventListener subscriptionListener;

    // DATA STORE
    //
    // adapter:                 ADAPTER FOR VIEW SERVER RECYCLER VIEW.
    //                          HANDLES STORAGE OF DISPLAYED SERVER DATA AS WELL.
    // LOG_OUT_MENU_ITEM_ID:    CONSTANT DECLARING ID FOR THE LOG OUT MENU ITEM.

    private ViewServerAdapter adapter;
    private final int LOG_OUT_MENU_ITEM_ID = -1;

    // VIEW OBJECTS
    //
    // ViewServerRecyclerView:  DISPLAYS ALL SERVERS A USER JOINED/OWNS. USES adapter AS ITS ADAPTER.
    // BottomToolbarAMV:        HANDLES THE MENU FOR THE BOTTOM TOOLBAR.
    // TopNavToolbar:           TOOLBAR OBJECT THAT HANDLES UPWARDS NAVIGATION AND THE TITLE
    // MainActionFAB:           BOTTOM TOOLBAR MAIN ACTION BUTTON. USED TO HANDLE MAIN ACTIONS ON THIS
    //                          ACTIVITY. BOUND TO NAVIGATING TO ADD SERVER ACTIVITY IN THIS INSTANCE.

    private RecyclerView ViewServerRecyclerView;
    private ActionMenuView BottomToolbarAMV;
    private Toolbar TopNavToolbar;
    private ImageButton MainActionFAB;

    // ACTIVITY STATE MANAGEMENT METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logHandler.printDefaultLog(LogHandler.STATE_ON_CREATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_servers);

        // BIND TOOLBARS
        // NOTE:    IT IS IMPORTANT TO GET THE INCLUDE VIEWS BEFORE DOING FIND VIEW BY ID.
        //          THIS ENSURES THAT ANDROID CAN ALWAYS FIND THE CORRECT VIEW OBJECT.

        View topNavView = findViewById(R.id.serversNavBarInclude);
        View bottomToolbarView = findViewById(R.id.serversBottomToolbarInclude);
        TopNavToolbar = (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
        BottomToolbarAMV = (ActionMenuView) bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
        MainActionFAB = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);

        logHandler.printDefaultLog(LogHandler.TOOLBAR_BOUND);

        // SETUP TOOLBARS
        this.setSupportActionBar(TopNavToolbar);
        TopNavToolbar.setTitle("View Servers");
        BottomToolbarAMV.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_add_white_24);
        MainActionFAB.setImageDrawable(icon);
        MainActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddServer();
            }
        });

        logHandler.printDefaultLog(LogHandler.TOOLBAR_SETUP);

        // BIND VIEW OBJECTS
        ViewServerRecyclerView = (RecyclerView) findViewById(R.id.viewServerRecyclerView);

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);

        // SETUP VIEW OBJECTS
        adapter = new ViewServerAdapter(new ArrayList<Server>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ViewServerRecyclerView.setLayoutManager(layoutManager);
        ViewServerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ViewServerRecyclerView.setAdapter(adapter);

        ViewServerRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(this, ViewServerRecyclerView, new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        handleTransitionIntoServer(position);
                    }
                })
        );

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_SETUP);

        // INITIALISE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null) {
            logHandler.printDefaultLog(LogHandler.FIREBASE_USER_NOT_FOUND);
            Intent backToLogin = new Intent(ViewServersActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            return;
        }
        logHandler.printDefaultLog(LogHandler.FIREBASE_USER_FOUND);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        logHandler.printDefaultLog(LogHandler.FIREBASE_INITIALISED);

        // INITIALISE LISTENERS

        // addServerOnce:   GETS AND ADDS A SINGLE SERVER TO adapter. SHOULD BE CALLED AS A SingleValueEvent FROM subscriptionListener.
        //                  CORRECT INVOCATION CODE: databaseRef.child("servers")
        //                                                      .child(serverId)
        //                                                      .addListenerForSingleValueEvent(addServerOnce)
        //                  SHOULD NOT BE USED INDEPENDENTLY.

        final ValueEventListener addServerOnce = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // CHECK THAT servers.child(serverId) DOES NOT HAVE A NULL VALUE (SERVER DOES EXIST)
                if (dataSnapshot.getValue() == null) {
                    logHandler.printDatabaseResultLog(
                            ".getValue()",
                            "Server Values",
                            "addServerOnce",
                            "null"
                    );
                    return;
                }

                Server server = dataSnapshot.getValue(Server.class);

                // CHECK THAT FIREBASE HAS SUCCESSFULLY CASTED THE VALUE OF THE dataSnapshot TO Server.
                if (server == null) {
                    logHandler.printLogWithMessage("Failed to parse server values as Server object!");
                    return;
                }

                logHandler.printDatabaseResultLog(
                        ".getValue()",
                        "Server Values",
                        "addServerOnce",
                        server.toString()
                );

                // ADD ID BACK TO server FROM THE dataSnapshot's KEY.
                server.set_id(dataSnapshot.getKey());

                // ADD SERVER TO adapter, THEN SORT AND TELL adapter TO UPDATE VIEW BASED ON NEW DATA.
                adapter.serverList.add(server);
                Collections.sort(adapter.serverList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        // subscriptionListener:    GETS ALL SUBSCRIBED (JOINED/OWNED) SERVERS FROM A USER AND CALLS addServerOnce FOR EACH SERVER ID.
        //                          ALSO GETS CALLED IF SERVERS ARE ADDED/REMOVED/CHANGED AND UPDATES adapter ACCORDINGLY.
        //                          CORRECT INVOCATION CODE: databaseRef.child("servers")
        //                                                              .child(currentUser.getUid())
        //                                                              .child("_subscribedServers")
        //                                                              .addChildEventListener(subscriptionListener)
        //                          SHOULD BE CANCELLED UPON ACTIVITY DESTROYED!

        subscriptionListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                logHandler.printLogWithMessage("Server added/loaded!");

                if (dataSnapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog(
                            ".getKey()",
                            "ServerID",
                            "subscriptionListener",
                            "null"
                    );
                    return;
                }

                String serverID = dataSnapshot.getKey();

                logHandler.printDatabaseResultLog(
                        ".getKey()",
                        "ServerID",
                        "subscriptionListener",
                        serverID
                );

                databaseRef.child("servers")
                           .child(serverID)
                           .addListenerForSingleValueEvent(addServerOnce);
            }

            // NOTE: This method is currently empty as there is no way to edit server name or details after
            //       server creation. May need to be implemented in future versions.

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                logHandler.printLogWithMessage("Server removed! Deleting chat message!");
                for(Server server : adapter.serverList) {
                    if(server.get_id().equals(dataSnapshot.getKey())) {
                        adapter.serverList.remove(server);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        logHandler.printDefaultLog(LogHandler.FIREBASE_LISTENERS_INITIALISED);

        databaseRef.child("users")
                   .child(currentUser.getUid())
                   .child("_subscribedServers")
                   .addChildEventListener(subscriptionListener);
    }

    @Override
    protected void onDestroy() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_DESTROY);

        // CANCEL CHILD EVENT LISTENERS ON ACTIVITY DESTROYED
        if (subscriptionListener != null) {
            databaseRef.removeEventListener(subscriptionListener);
        }

        // RESET VIEW SERVERS MENU ITEM
        Utils.ToggleMenuItemAlpha(
                this,
                R.id.viewServersMenuItem,
                "View Servers",
                R.drawable.round_home_white_36,
                "round_home_white_36",
                true,
                logHandler
        );

        super.onDestroy();
    }

    // CLASS METHODS

    private void handleTransitionIntoServer(Integer position) {
        logHandler.printLogWithMessage("User tapped on a server!");
        final HashMap<String, String> extraMap = new HashMap<String, String>();
        extraMap.put("SERVER_ID", adapter.serverList.get(position).get_id());

        final ValueEventListener checkIfOwner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ownerID = (String) dataSnapshot.child("_ownerID").getValue();
                extraMap.put("IS_OWNER", ((Boolean) currentUser.getUid().equals(ownerID)).toString());

                Utils.StartActivityOnNewStack(
                        ViewServersActivity.this,
                        PostsActivity.class,
                        "Posts Activity",
                        extraMap,
                        logHandler);

                onStop();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        databaseRef.child("servers")
                .child(adapter.serverList.get(position).get_id())
                .addListenerForSingleValueEvent(checkIfOwner);
    }

    private void handleAddServer() {
        logHandler.printLogWithMessage("User tapped on Add Server!");

        Intent transitionToAddServer = new Intent(ViewServersActivity.this, AddServerActivity.class);
        startActivity(transitionToAddServer);
        logHandler.printActivityIntentLog("AddServer Activity");
        onStop();
    }

    // NAME:                toggleOwnMenuItemDisplay
    // DESCRIPTION:         TOGGLES THE MENU ITEM'S VISUAL STATE FOR THE CURRENT ACTIVITY
    // INPUTS:
    // isEnabled:           WHEN TRUE, SETS THE MENU ITEM TO FULL OPACITY, OTHERWISE SETS IT TO 40%
    // RETURN VALUE:        NULL

    // TOOLBAR OVERRIDE METHODS

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Make ViewServers Button on menu bar look disabled
        Utils.ToggleMenuItemAlpha(
                this,
                R.id.viewServersMenuItem,
                "View Servers",
                R.drawable.round_home_white_36,
                "round_home_white_36",
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
                // DISABLED
                return false;

            case R.id.viewProfileMenuItem:
                logHandler.printLogWithMessage("User tapped on ViewProfile Menu Item!");

                Utils.StartActivityOnNewStack(
                        ViewServersActivity.this,
                        ViewProfileActivity.class,
                        "View Profile Activity",
                        null,
                        logHandler);

                // Reset disabled ActionMenuItemView button back to normal state
                Utils.ToggleMenuItemAlpha(
                        this,
                        R.id.viewServersMenuItem,
                        "View Servers",
                        R.drawable.round_home_white_36,
                        "round_home_white_36",
                        true,
                        logHandler
                );

                finish();
                return true;

            case LOG_OUT_MENU_ITEM_ID:
                logHandler.printLogWithMessage("User tapped on Log Out Menu Item!");

                firebaseAuth.signOut();
                Intent logOutToSignIn = new Intent(ViewServersActivity.this, LoginActivity.class);
                startActivity(logOutToSignIn);
                logHandler.printActivityIntentLog("Login Activity");

                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
