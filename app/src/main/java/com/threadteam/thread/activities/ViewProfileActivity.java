package com.threadteam.thread.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.adapters.ProfileAdapter;
import com.threadteam.thread.abstracts.MainBaseActivity;
import com.threadteam.thread.adapters.ViewPostDetailsAdapter;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This activity class handles the viewing of a post and commenting on the post
 *
 * @author Danny Chan Yu Tian
 * @version 2.0
 * @since 1.0
 */

public class ViewProfileActivity extends MainBaseActivity {

    // DATA STORE
    /** Adapter object for ProfileRecyclerView. */
    private ProfileAdapter adapter;

    // VIEW OBJECTS
    /**
     * Displays profile details and servers of the user.
     * Uses ProfileAdapter as its adapter.
     * @see ProfileAdapter
     */
    private RecyclerView ProfileRecyclerView;

    // INITIALISE LISTENERS
    /**
     *  Retrieves the real-time details of a server for a server id and stores it in the adapter.
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
    ValueEventListener getUserData = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "User Values", "getUserData", "null");
                return;
            }

            String id = (String) dataSnapshot.getKey();
            String username = (String) dataSnapshot.child("_username").getValue();
            String profileImageURL = (String) dataSnapshot.child("_profileImageURL").getValue();
            String aboutUsMsg = (String) dataSnapshot.child("_aboutUsMessage").getValue();
            String statusMsg = (String) dataSnapshot.child("_statusMessage").getValue();
            String token = (String) dataSnapshot.child("_token").getValue();


            if(id == null) {
                logHandler.printDatabaseResultLog(".getKey()", "User ID", "getUserData", "null");
                return;
            }
            else if(username == null) {
                logHandler.printDatabaseResultLog(".child(\"_username\").getValue()", "Username", "getUserData", "null");
                return;
            }
            else if(aboutUsMsg == null) {
                logHandler.printDatabaseResultLog(".child(\"_aboutUsMessage\").getValue()", "About Us Message", "getUserData", "null");
                return;
            }
            else if(statusMsg == null) {
                logHandler.printDatabaseResultLog(".child(\"_statusMessage\").getValue()", "Status Message", "getUserData", "null");
                return;
            }
            else if(profileImageURL == null) {
                logHandler.printDatabaseResultLog(".child(\"_profileImageURL\").getValue()", "Profile Image URL", "getUserData", "null");
            }

            logHandler.printDatabaseResultLog(".getKey()", "User ID", "addUserOnce", id);
            logHandler.printDatabaseResultLog(".child(\"_username\").getValue()", "Username", "getUserData", username);
            logHandler.printDatabaseResultLog(".child(\"_aboutUsMessage\").getValue()", "About Us Message", "getUserData", aboutUsMsg);
            logHandler.printDatabaseResultLog(".child(\"_statusMessage\").getValue()", "Status Message", "getUserData", statusMsg);
            if (profileImageURL != null) {
                logHandler.printDatabaseResultLog(".child(\"_profileImageURL\").getValue()", "Profile Image URL", "getUserData", profileImageURL);
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
                }

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
    protected int setLayoutIDForContentView() {
        return R.layout.activity_view_profile;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return ViewProfileActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "View Profile";
    }

    @Override
    protected ImageButton setMainActionButton() {
        View bottomToolbarView = findViewById(R.id.profileBottomToolbarInclude);
        ImageButton mainActionButton = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);
        Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_create_white_24);
        mainActionButton.setImageDrawable(icon);
        mainActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToEditProfile = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
                startActivity(goToEditProfile);
                logHandler.printActivityIntentLog("Edit Profile Activity");
            }
        });
        return mainActionButton;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.profileNavBarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return R.id.profileBottomToolbarInclude;
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

    @Override
    protected void AttachOnStartListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .addValueEventListener(getUserData);
    }

    @Override
    protected void DestroyOnStartListeners() {
        if(getUserData != null) {
            databaseRef.removeEventListener(getUserData);
        }
        if(getServerDetails != null) {
            databaseRef.removeEventListener(getServerDetails);
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return R.id.viewProfileMenuItem;
    }

}
