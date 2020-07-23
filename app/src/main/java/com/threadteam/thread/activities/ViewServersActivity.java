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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.RecyclerTouchListener;
import com.threadteam.thread.adapters.ViewServerAdapter;
import com.threadteam.thread.abstracts.MainBaseActivity;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.models.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This activity class displays all the servers that the current user is in.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 */

public class ViewServersActivity extends MainBaseActivity {

    // DATA STORE

    /** Adapter object for ViewServerRecyclerView. */
    private ViewServerAdapter adapter;

    // VIEW OBJECTS
    //
    // ViewServerRecyclerView:  DISPLAYS ALL SERVERS A USER JOINED/OWNS. USES adapter AS ITS ADAPTER.

    /**
     * Displays all members currently in the server, along with selected details.
     * Uses ViewServerAdapter as its adapter.
     * @see ViewServerAdapter
     */

    private RecyclerView ViewServerRecyclerView;

    // FIREBASE

    /** Stores a map of all listener-spawned value event listeners to be destroyed later */
    private HashMap<DatabaseReference, ValueEventListener> listenerHashMap = new HashMap<>();

    // LISTENERS

    /**
     *  Retrieves server details for a serverId and loads them into the adapter.
     *
     *  Database Path:      root/servers/(serverId)
     *  Usage:              ValueEventListener
     */

    final ValueEventListener getServerDetails = new ValueEventListener() {
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

    /**
     *  Handles the attaching of value event listeners to the current user's subscribed servers to get their details.
     *
     *  Database Path:      root/users/(currentUser.getUid())/_subscribedServers
     *  Usage:              ChildEventListener
     */

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
                       .addValueEventListener(getServerDetails);

            listenerHashMap.put(databaseRef.child("servers").child(serverID), getServerDetails);
        }

        // Server detail updates are handled by getServerDetails

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

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

            logHandler.printLogWithMessage("Server removed! Deleting chat message!");
            for(Server server : adapter.serverList) {
                if(server.get_id().equals(dataSnapshot.getKey())) {
                    adapter.serverList.remove(server);
                    adapter.notifyDataSetChanged();

                    // Detach listener
                    databaseRef.child("servers").child(serverID).removeEventListener(getServerDetails);
                    listenerHashMap.remove(databaseRef.child("servers").child(serverID));
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
    protected AppCompatActivity setCurrentActivity() {
        return ViewServersActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "View Servers";
    }

    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_view_servers;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.serversNavBarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return R.id.serversBottomToolbarInclude;
    }

    @Override
    protected ImageButton setMainActionButton() {
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
    protected void BindViewObjects() {
        ViewServerRecyclerView = (RecyclerView) findViewById(R.id.viewServerRecyclerView);
    }

    @Override
    protected void SetupViewObjects() {
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
    protected void AttachListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .child("_subscribedServers")
                .addChildEventListener(subscriptionListener);
    }

    @Override
    protected void DestroyListeners() {
        if (subscriptionListener != null) {
            databaseRef.child("users")
                       .child(currentUser.getUid())
                       .child("_subscribedServers")
                       .removeEventListener(subscriptionListener);
        }
        if(getServerDetails != null) {
            for(DatabaseReference ref : listenerHashMap.keySet()) {
                ref.removeEventListener(listenerHashMap.get(ref));
            }
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return R.id.viewServersMenuItem;
    }

    // ACTIVITY SPECIFIC METHODS

    /**
     * Sends user to the Add Server Activity.
     */

    private void handleAddServer() {
        logHandler.printLogWithMessage("User tapped on Add Server!");

        Intent transitionToAddServer = new Intent(ViewServersActivity.this, AddServerActivity.class);
        startActivity(transitionToAddServer);
        logHandler.printActivityIntentLog("Add Server Activity");
        onStop();
    }

    /**
     * Sends user to the Posts Activity as a landing page for the set of server activities.
     * Calculates and adds in the SERVER_ID and IS_OWNER extras as well.
     * @param position The index of the server the user clicked on.
     */

    private void handleTransitionIntoServer(final Integer position) {
        logHandler.printLogWithMessage("User tapped on a server!");

        final ValueEventListener checkIfOwner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ownerID = (String) dataSnapshot.child("_ownerID").getValue();

                Intent goToPosts = new Intent(currentActivity, PostsActivity.class);
                goToPosts.putExtra("SERVER_ID", adapter.serverList.get(position).get_id());
                goToPosts.putExtra("IS_OWNER", currentUser.getUid().equals(ownerID));
                currentActivity.startActivity(goToPosts);
                logHandler.printActivityIntentLog("Posts");
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
