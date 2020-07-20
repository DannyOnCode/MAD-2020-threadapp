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
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.adapters.MemberProfileAdapter;
import com.threadteam.thread.adapters.ViewMemberAdapter;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.models.User;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemberProfileActivity extends _ServerBaseActivity {

    // DATA STORE
    //
    // adapter:                 ADAPTER FOR PROFILE RECYCLER VIEW.
    // memberId:                CONTAINS THE ID OF THE MEMBER TO DISPLAY THE PROFILE OF

    private MemberProfileAdapter adapter;
    private String memberId;

    // VIEW OBJECTS
    //
    // ProfileRecyclerView:     DISPLAYS PROFILE DETAILS FOR SERVER MEMBER. USES adapter AS ITS ADAPTER.

    private RecyclerView ProfileRecyclerView;

    // INITIALISE LISTENERS

    // getServerDetails:    GETS AND ADDS A SINGLE SERVER TO adapter. SHOULD BE CALLED AS A SingleValueEvent FROM subscriptionListener.
    //                      CORRECT INVOCATION CODE: databaseRef.child("servers")
    //                                                          .child(serverId)
    //                                                          .addValueEventListener(getServerDetails)
    //                      SHOULD NOT BE USED INDEPENDENTLY.

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

    // getMemberProfile:    USED TO RETRIEVE USER DATA
    //                      CORRECT INVOCATION CODE: databaseRef.child("users")
    //                                                          .child(memberID)
    //                                                          .addValueEventListener(getMemberProfile)
    //                      SHOULD NOT BE USED INDEPENDENTLY.

    ValueEventListener getMemberProfile = new ValueEventListener() {
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

            adapter.userData = new User(id, username, profileImageURL, aboutUsMsg, statusMsg, servers, expList);
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
    ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseViewProfileConstraintLayout);
    }

    @Override
    int setLayoutIDForContentView() {
        return R.layout.activity_view_profile;
    }

    @Override
    AppCompatActivity setCurrentActivity() {
        return MemberProfileActivity.this;
    }

    @Override
    String setTitleForActivity() {
        return "View Member Profile";
    }

    @Override
    ImageButton setMainActionButton() {
        View bottomToolbarView = findViewById(R.id.profileBottomToolbarInclude);
        ImageButton mainActionButton = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);
        mainActionButton.setVisibility(View.GONE);
        return mainActionButton;
    }

    @Override
    Toolbar setTopNavToolbar() {
        View topNavView = findViewById(R.id.profileNavBarInclude);
        return (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
    }

    @Override
    ActionMenuView setBottomToolbarAMV() {
        View bottomToolbarView = findViewById(R.id.profileBottomToolbarInclude);
        return (ActionMenuView) bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
    }

    @Override
    void BindViewObjects() {
        ProfileRecyclerView = (RecyclerView) findViewById(R.id.viewProfileRecyclerView);
    }

    @Override
    void SetupViewObjects() {
        adapter = new MemberProfileAdapter(null, new ArrayList<Server>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ProfileRecyclerView.setLayoutManager(layoutManager);
        ProfileRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ProfileRecyclerView.setAdapter(adapter);
    }

    @Override
    void HandleAdditionalIntentExtras() {
        final Intent dataReceiver = getIntent();
        String MEMBER_ID_KEY = "MEMBER_ID";

        memberId = dataReceiver.getStringExtra(MEMBER_ID_KEY);

        if (memberId == null) {
            logHandler.printGetExtrasResultLog(MEMBER_ID_KEY, "null");

            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            returnToViewMembers();

        } else {
            logHandler.printGetExtrasResultLog(MEMBER_ID_KEY, memberId);
        }
    }

    @Override
    void AttachListeners() {
        databaseRef.child("users")
                   .child(memberId)
                   .addValueEventListener(getMemberProfile);
    }

    @Override
    void DestroyListeners() {
        if(getMemberProfile != null) {
            databaseRef.removeEventListener(getMemberProfile);
        }
        if(getServerDetails != null) {
            databaseRef.removeEventListener(getServerDetails);
        }
    }

    @Override
    int setCurrentMenuItemID() {
        return R.id.membersMenuItem;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            logHandler.printLogWithMessage("User tapped on Back Button!");
            returnToViewMembers();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ACTIVITY SPECIFIC METHODS

    private void returnToViewMembers() {
        Intent goToViewMembers = new Intent(currentActivity, ViewMembersActivity.class);
        PutExtrasForServerIntent(goToViewMembers);
        currentActivity.startActivity(goToViewMembers);
        logHandler.printActivityIntentLog("View Members");
    }
}
