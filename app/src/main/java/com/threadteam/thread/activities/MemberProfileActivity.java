package com.threadteam.thread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.adapters.ProfileAdapter;
import com.threadteam.thread.abstracts.ServerBaseActivity;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This activity class displays the profile of a server member.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 */

public class MemberProfileActivity extends ServerBaseActivity {

    // DATA STORE

    /** Adapter object for ProfileRecyclerView. */
    private ProfileAdapter adapter;

    /** Stores the id of the member whose profile is to be displayed. */
    private String memberId;

    // VIEW OBJECTS

    /**
     * Handles the display of the member's profile.
     * Uses ProfileAdapter as its adapter.
     * @see ProfileAdapter
     */

    private RecyclerView ProfileRecyclerView;

    // FIREBASE

    /** Stores a map of all listener-spawned value event listeners to be destroyed later */
    private HashMap<DatabaseReference, ValueEventListener> listenerHashMap = new HashMap<>();

    // INITIALISE LISTENERS

    /**
     *  Retrieves the real-time details of a server for a server id and stores it in the adapter.
     *
     *  Database Path:      root/servers/(serverId)
     *  Usage:              ValueEventListener
     */

    private final ValueEventListener getServerDetails = new ValueEventListener() {
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

            // Check if server was removed
            if(!adapter.userData.get_subscribedServers().contains(server.get_id())) {
                return;
            }

            boolean serverUpdated = false;

            // CHECK IF SERVER IS ALREADY DISPLAYED. UPDATE IF ALREADY DISPLAYED
            for(int i=0; i<adapter.serverList.size(); i++) {
                if(adapter.serverList.get(i).get_id().equals(server.get_id())) {
                    adapter.serverList.set(i, server);
                    serverUpdated = true;
                    break;
                }
            }

            // ELSE ADD SERVER TO adapter, THEN SORT AND TELL adapter TO UPDATE VIEW BASED ON NEW DATA.

            if(!serverUpdated){
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
     *  Retrieves the member's user data and stores it in the adapter.
     *
     *  Database Path:      root/users/(memberID)
     *  Usage:              ValueEventListener
     */

    private ValueEventListener getMemberProfile = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "User Values", "getMemberProfile", "null");
                return;
            }

            String id = (String) dataSnapshot.getKey();
            String username = (String) dataSnapshot.child("_username").getValue();
            String profileImageURL = (String) dataSnapshot.child("_profileImageURL").getValue();
            String aboutUsMsg = (String) dataSnapshot.child("_aboutUsMessage").getValue();
            String statusMsg = (String) dataSnapshot.child("_statusMessage").getValue();
            String token = (String) dataSnapshot.child("_token").getValue();

            if(id == null) {
                logHandler.printDatabaseResultLog(".getKey()", "User ID", "getMemberProfile", "null");
                return;
            }
            else if(username == null) {
                logHandler.printDatabaseResultLog(".child(\"_username\").getValue()", "Username", "getMemberProfile", "null");
                return;
            }
            else if(aboutUsMsg == null) {
                logHandler.printDatabaseResultLog(".child(\"_aboutUsMessage\").getValue()", "About Us Message", "getMemberProfile", "null");
                return;
            }
            else if(statusMsg == null) {
                logHandler.printDatabaseResultLog(".child(\"_statusMessage\").getValue()", "Status Message", "getMemberProfile", "null");
                return;
            }
            else if(profileImageURL == null) {
                logHandler.printDatabaseResultLog(".child(\"_profileImageURL\").getValue()", "Profile Image URL", "getMemberProfile", "null");
            }

            logHandler.printDatabaseResultLog(".getKey()", "User ID", "addUserOnce", id);
            logHandler.printDatabaseResultLog(".child(\"_username\").getValue()", "Username", "getMemberProfile", username);
            logHandler.printDatabaseResultLog(".child(\"_aboutUsMessage\").getValue()", "About Us Message", "getMemberProfile", aboutUsMsg);
            logHandler.printDatabaseResultLog(".child(\"_statusMessage\").getValue()", "Status Message", "getMemberProfile", statusMsg);
            if (profileImageURL != null) {
                logHandler.printDatabaseResultLog(".child(\"_profileImageURL\").getValue()", "Profile Image URL", "getMemberProfile", profileImageURL);
            }

            List<String> servers = new ArrayList<>();
            List<Integer> expList = new ArrayList<>();

            for(DataSnapshot snapshot : dataSnapshot.child("_subscribedServers").getChildren()) {

                String _serverId = snapshot.getKey();
                if(_serverId == null) {
                    logHandler.printDatabaseResultLog(
                            ".child(\"_subscribedServers\").getChildren().getKey()",
                            "Server ID",
                            "addUserOnce",
                            "null");
                    continue;
                }
                logHandler.printDatabaseResultLog(
                        ".child(\"_subscribedServers\").getChildren().getKey()",
                        "Server ID",
                        "addUserOnce",
                        _serverId);

                if(snapshot.getValue() == null) {
                    logHandler.printDatabaseResultLog(
                            ".child(\"_subscribedServers\").getChildren().getValue()",
                            "Server EXP",
                            "addUserOnce",
                            "null");
                    continue;
                }
                logHandler.printDatabaseResultLog(
                        ".child(\"_subscribedServers\").getChildren().getKey()",
                        "Server ID",
                        "addUserOnce",
                        ((Long) snapshot.getValue()).toString());

                servers.add(snapshot.getKey());
                expList.add(((Long) snapshot.getValue()).intValue());
            }

            adapter.userData = new User(id, username, profileImageURL, aboutUsMsg, statusMsg, token, servers, expList);

            List<Server> oldServerList = adapter.serverList;

            for(String _serverId : adapter.userData.get_subscribedServers()) {
                // ADD VALUE EVENT LISTENER TO SERVER FOR SERVER ID IF USER DID NOT ALREADY HAVE THIS SERVER LOADED
                boolean isNew = true;

                if(adapter.serverList != null) {
                    for(Server s : adapter.serverList) {
                        if(s.get_id().equals(_serverId)) {
                            isNew = false;
                            break;
                        }
                    }
                }

                if(isNew) {
                    databaseRef.child("servers")
                            .child(_serverId)
                            .addValueEventListener(getServerDetails);

                    listenerHashMap.put(databaseRef.child("servers").child(_serverId), getServerDetails);
                }
            }

            // Check if a server was removed
            if(oldServerList != null && oldServerList.size() > 0) {
                for(Server oldServer : oldServerList) {
                    // If the server was removed
                    if(!adapter.userData.get_subscribedServers().contains(oldServer.get_id())) {
                        // Detach old listener
                        databaseRef.child("servers")
                                   .child(oldServer.get_id())
                                   .removeEventListener(getServerDetails);

                        listenerHashMap.remove(databaseRef.child("servers").child(oldServer.get_id()));

                        // Remove server from server list
                        for(int i=0; i<adapter.serverList.size(); i++) {
                            if(adapter.serverList.get(i).get_id().equals(oldServer.get_id())) {
                                adapter.serverList.remove(i);
                                adapter.notifyDataSetChanged();
                                return;
                            }
                        }
                    }
                }
            }

            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
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

    // ABSTRACT OVERRIDE METHODS

    @Override
    protected ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseViewProfileConstraintLayout);
    }

    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_view_profile;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return MemberProfileActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "View Member Profile";
    }

    @Override
    protected ImageButton setMainActionButton() {
        View bottomToolbarView = findViewById(R.id.profileBottomToolbarInclude);
        ImageButton mainActionButton = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);
        mainActionButton.setVisibility(View.GONE);
        return mainActionButton;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.profileNavBarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return null;
    }

    @Override
    protected void BindViewObjects() {
        ProfileRecyclerView = (RecyclerView) findViewById(R.id.viewProfileRecyclerView);
    }

    @Override
    protected void SetupViewObjects() {
        adapter = new ProfileAdapter(null, new ArrayList<Server>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ProfileRecyclerView.setLayoutManager(layoutManager);
        ProfileRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ProfileRecyclerView.setAdapter(adapter);
    }

    /**
     * {@inheritDoc}
     * This implementation retrieves an additional extra MEMBER_ID from the intent.
     */

    @Override
    protected void HandleAdditionalIntentExtras() {
        final Intent dataReceiver = getIntent();
        String MEMBER_ID_KEY = "MEMBER_ID";

        memberId = dataReceiver.getStringExtra(MEMBER_ID_KEY);

        if (memberId == null) {
            logHandler.printGetExtrasResultLog(MEMBER_ID_KEY, "null");

            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            finish();

        } else {
            logHandler.printGetExtrasResultLog(MEMBER_ID_KEY, memberId);
        }
    }

    @Override
    protected void AttachOnStartListeners() {
        databaseRef.child("users")
                   .child(memberId)
                   .addValueEventListener(getMemberProfile);
    }

    @Override
    protected void DestroyOnStartListeners() {
        if(getMemberProfile != null) {
            databaseRef.child("users").child(memberId).removeEventListener(getMemberProfile);
        }
        if(getServerDetails != null) {
            for(DatabaseReference ref : listenerHashMap.keySet()) {
                ref.removeEventListener(listenerHashMap.get(ref));
            }
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return R.id.membersMenuItem;
    }

    /**
     * {@inheritDoc}
     * This implementation overrides the back button to send the user back to View Members instead of View Servers
     */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            logHandler.printLogWithMessage("User tapped on Back Button!");
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
