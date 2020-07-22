package com.threadteam.thread.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.RecyclerTouchListener;
import com.threadteam.thread.Utils;
import com.threadteam.thread.adapters.ViewServerAdapter;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.models.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ViewServersActivity extends _MainBaseActivity {

    // DATA STORE
    //
    // adapter:                 ADAPTER FOR VIEW SERVER RECYCLER VIEW.
    //                          HANDLES STORAGE OF DISPLAYED SERVER DATA AS WELL.

    private ViewServerAdapter adapter;

    // VIEW OBJECTS
    //
    // ViewServerRecyclerView:  DISPLAYS ALL SERVERS A USER JOINED/OWNS. USES adapter AS ITS ADAPTER.

    private RecyclerView ViewServerRecyclerView;

    // LISTENERS

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

            // CHECK IF SERVER IS ALREADY DISPLAYED
            boolean serverIsDisplayed = false;

            for(int i=0; i<adapter.serverList.size(); i++) {
                if(adapter.serverList.get(i).get_id().equals(server.get_id())) {
                    adapter.serverList.set(i, server);
                    serverIsDisplayed = true;
                    break;
                }
            }

            // ADD SERVER TO adapter, THEN SORT AND TELL adapter TO UPDATE VIEW BASED ON NEW DATA.

            if(!serverIsDisplayed) {
                adapter.serverList.add(server);
            }

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

    final ChildEventListener subscriptionListener = new ChildEventListener() {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // ABSTRACT OVERRIDE METHODS

    @Override
    AppCompatActivity setCurrentActivity() {
        return ViewServersActivity.this;
    }

    @Override
    String setTitleForActivity() {
        return "View Servers";
    }

    @Override
    int setLayoutIDForContentView() {
        return R.layout.activity_view_servers;
    }

    @Override
    Toolbar setTopNavToolbar() {
        View topNavView = findViewById(R.id.serversNavBarInclude);
        return (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
    }

    @Override
    ActionMenuView setBottomToolbarAMV() {
        View bottomToolbarView = findViewById(R.id.serversBottomToolbarInclude);
        return (ActionMenuView) bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
    }

    @Override
    ImageButton setMainActionButton() {
        View bottomToolbarView = findViewById(R.id.serversBottomToolbarInclude);
        ImageButton mainActionButton = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_add_white_24);
        mainActionButton.setImageDrawable(icon);

        mainActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddServer();
            }
        });

        return mainActionButton;
    }

    @Override
    void BindViewObjects() {
        ViewServerRecyclerView = (RecyclerView) findViewById(R.id.viewServerRecyclerView);
    }

    @Override
    void SetupViewObjects() {
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
    }

    @Override
    void AttachListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .child("_subscribedServers")
                .addChildEventListener(subscriptionListener);
    }

    @Override
    void DestroyListeners() {
        if (subscriptionListener != null) {
            databaseRef.child("users")
                       .child(currentUser.getUid())
                       .child("_subscribedServers")
                       .removeEventListener(subscriptionListener);
        }
    }

    @Override
    int setCurrentMenuItemID() {
        return R.id.viewServersMenuItem;
    }

    // ACTIVITY SPECIFIC METHODS

    private void handleAddServer() {
        logHandler.printLogWithMessage("User tapped on Add Server!");

        Intent transitionToAddServer = new Intent(ViewServersActivity.this, AddServerActivity.class);
        startActivity(transitionToAddServer);
        logHandler.printActivityIntentLog("Add Server Activity");
        onStop();
    }

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
}
