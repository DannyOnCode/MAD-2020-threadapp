package com.threadteam.thread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.threadteam.thread.adapters.EditMemberTitleAdapter;
import com.threadteam.thread.adapters.ViewMemberAdapter;
import com.threadteam.thread.abstracts.ServerBaseActivity;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This activity class displays all the members in the current server.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 */

public class ViewMembersActivity extends ServerBaseActivity {

    // DATA STORE

    /** Adapter object for ViewMembersRecyclerView. */
    private ViewMemberAdapter adapter;

    // VIEW OBJECTS

    /**
     * Displays all members currently in the server, along with selected details.
     * Uses ViewMemberAdapter as its adapter.
     * @see ViewMemberAdapter
     */

    private RecyclerView ViewMembersRecyclerView;

    // FIREBASE

    /** Stores a map of all listener-spawned value event listeners to be destroyed later */
    private HashMap<DatabaseReference, ValueEventListener> listenerHashMap = new HashMap<>();

    // INITIALISE LISTENERS

    /**
     *  Retrieves all member titles for the current server and loads it into the adapter.
     *
     *  Database Path:      root/titles/(serverId)
     *  Usage:              ValueEventListener
     */

    private ValueEventListener retrieveMemberTitles = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Member Title Data", "retrieveMemberTitles", "null");
                return;
            }

            List<String> titleData = new ArrayList<>(Collections.nCopies(10, ""));

            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (snapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog("snapshot.getKey()", "Member Title Index", "retrieveMemberTitles", "null");
                    return;
                }

                int index = Integer.parseInt(snapshot.getKey());
                logHandler.printDatabaseResultLog("snapshot.getValue()", "Member Title", "retrieveMemberTitles", Integer.toString(index));

                String title = (String) snapshot.getValue();
                if (title == null) {
                    logHandler.printDatabaseResultLog("snapshot.getValue()", "Member Title", "retrieveMemberTitles", "null");
                    return;
                }
                logHandler.printDatabaseResultLog("snapshot.getValue()", "Member Title", "retrieveMemberTitles", title);

                titleData.set(index, title);
            }

            adapter.titleList = titleData;
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    /**
     *  Retrieves all user details for a user id and loads it into the adapter. Sorts users by their experience.
     *
     *  Database Path:      root/users/(userID)
     *  Usage:              ValueEventListener
     */

    private ValueEventListener getUserDetails = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "User Values", "getUserDetails", "null");
                return;
            }

            String id = (String) dataSnapshot.getKey();
            String username = (String) dataSnapshot.child("_username").getValue();
            String profileImageURL = (String) dataSnapshot.child("_profileImageURL").getValue();
            String token = (String) dataSnapshot.child("_token").getValue();

            if(id == null) {
                logHandler.printDatabaseResultLog(".getKey()", "User ID", "getUserDetails", "null");
                return;
            }
            else if(username == null) {
                logHandler.printDatabaseResultLog(".child(\"_username\").getValue()", "Username", "getUserDetails", "null");
                return;
            }
            else if(profileImageURL == null) {
                logHandler.printDatabaseResultLog(".child(\"_profileImageURL\").getValue()", "Profile Image URL", "getUserDetails", "null");
            }

            logHandler.printDatabaseResultLog(".getKey()", "User ID", "addUserOnce", id);
            logHandler.printDatabaseResultLog(".child(\"_username\").getValue()", "Username", "getUserDetails", username);
            logHandler.printDatabaseResultLog(".child(\"_profileImageURL\").getValue()", "Profile Image URL", "getUserDetails", profileImageURL);


            List<String> servers = new ArrayList<>();
            List<Integer> expList = new ArrayList<>();

            for(DataSnapshot snapshot : dataSnapshot.child("_subscribedServers").getChildren()) {

                if(snapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog(
                            ".child(\"_subscribedServers\").getChildren().getKey()",
                            "Server ID",
                            "getUserDetails",
                            "null");
                    break;
                }
                logHandler.printDatabaseResultLog(
                        ".child(\"_subscribedServers\").getChildren().getKey()",
                        "Server ID",
                        "getUserDetails",
                        snapshot.getKey());

                if(snapshot.getValue() == null) {
                    logHandler.printDatabaseResultLog(
                            ".child(\"_subscribedServers\").getChildren().getValue()",
                            "Server EXP",
                            "getUserDetails",
                            "null");
                    break;
                }
                logHandler.printDatabaseResultLog(
                        ".child(\"_subscribedServers\").getChildren().getKey()",
                        "Server ID",
                        "getUserDetails",
                        ((Long) snapshot.getValue()).toString());

                servers.add(snapshot.getKey());
                expList.add(((Long) snapshot.getValue()).intValue());
            }

            User newUser = new User(id, username, profileImageURL, "", "",token, servers, expList);

            // Check if user left
            if(!newUser.get_subscribedServers().contains(serverId)) {
                return;
            }

            // Pre-exist check
            for(int i=0; i<adapter.userList.size(); i++) {
                if(adapter.userList.get(i).get_id().equals(newUser.get_id())) {
                    adapter.userList.set(i, newUser);
                    if(i>0) i+= 1;
                    adapter.notifyItemChanged(i);
                    return;
                }
            }

            String currUid = currentUser.getUid();

            // Insertion sort by exp that floats current user to top
            for(int i=0; i<adapter.userList.size(); i++) {
                if(id.equals(currUid) ||
                        newUser.GetUserExpForServer(serverId) > adapter.userList.get(i).GetUserExpForServer(serverId) &&
                                !adapter.userList.get(i).get_id().equals(currUid)) {
                    adapter.userList.add(i, newUser);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }

            // If is smallest, then add to back
            int s = adapter.userList.size();
            adapter.userList.add(s, newUser);
            adapter.notifyItemInserted(s);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    // memberListener:  HANDLES LOADING OF ALL MEMBERS, AS WELL AS UPDATING THE ADAPTER
    //                  ON MEMBERS ADDED/DELETED/CHANGED EVENTS
    //                  CORRECT INVOCATION CODE: databaseRef.child("members")
    //                                                      .child(serverId)
    //                                                      .addChildEventListener(memberListener)
    //                  SHOULD BE CANCELLED UPON ACTIVITY STOP!

    /**
     *  Handles the attaching of value event listeners to all members to get their user details.
     *
     *  Database Path:      root/users/(userID)
     *  Usage:              ValueEventListener
     */

    private ChildEventListener memberListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            logHandler.printLogWithMessage("Member added/loaded!");

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Member ID", "memberListener", "null");
                return;
            }
            logHandler.printDatabaseResultLog(".getKey()", "Member ID", "memberListener", dataSnapshot.getKey());

            String userID = dataSnapshot.getKey();

            databaseRef.child("users")
                    .child(userID)
                    .addValueEventListener(getUserDetails);

            listenerHashMap.put(databaseRef.child("users").child(userID), getUserDetails);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // This is handled by the getUser listener.
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            logHandler.printLogWithMessage("Member removed! Deleting member from adapter!");

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Member ID", "memberListener", "null");
                return;
            }
            logHandler.printDatabaseResultLog(".getKey()", "Member ID", "memberListener", dataSnapshot.getKey());

            String userID = dataSnapshot.getKey();

            for(int i=0; i<adapter.userList.size(); i++) {
                if(adapter.userList.get(i).get_id() != null && adapter.userList.get(i).get_id().equals(userID)) {
                    adapter.userList.remove(i);
                    adapter.notifyItemRemoved(i);

                    // DETACH LISTENER
                    databaseRef.child("users")
                               .child(userID)
                               .removeEventListener(getUserDetails);

                    listenerHashMap.remove(databaseRef.child("users").child(userID));
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

    // ABSTRACT OVERRIDE METHODS

    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_view_members;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return ViewMembersActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "View Members";
    }

    @Override
    protected ImageButton setMainActionButton() {
        View bottomToolbarView = findViewById(R.id.viewMembersBottomToolbarInclude);
        ImageButton mainActionButton = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);
        mainActionButton.setVisibility(View.GONE);
        return mainActionButton;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.viewMembersNavBarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return R.id.viewMembersBottomToolbarInclude;
    }

    @Override
    protected void BindViewObjects() {
        ViewMembersRecyclerView = (RecyclerView) findViewById(R.id.viewMembersRecyclerView);
    }

    @Override
    protected ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseViewMembersConstraintLayout);
    }

    @Override
    protected void SetupViewObjects() {
        adapter = new ViewMemberAdapter(new ArrayList<User>(), "");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ViewMembersRecyclerView.setLayoutManager(layoutManager);
        ViewMembersRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ViewMembersRecyclerView.setAdapter(adapter);

        ViewMembersRecyclerView.addOnItemTouchListener(
            new RecyclerTouchListener(this, ViewMembersRecyclerView, new RecyclerViewClickListener() {
                @Override
                public void onClick(View view, int position) {
                if(position != 1) {
                    if(position > 1) {
                        position -= 1;
                    }

                    String memberId = adapter.userList.get(position).get_id();

                    Intent goToMemberProfile = new Intent(currentActivity, MemberProfileActivity.class);
                    PutExtrasForServerIntent(goToMemberProfile);
                    goToMemberProfile.putExtra("MEMBER_ID", memberId);
                    currentActivity.startActivity(goToMemberProfile);
                    logHandler.printActivityIntentLog("View Member Profile");
                }
                }
            })
        );
    }

    /**
     * {@inheritDoc}
     * This implementation loads serverId into the adapter.
     */

    @Override
    protected void DoAdditionalSetupForFirebase() {
        adapter.serverId = serverId;
    }

    @Override
    protected void AttachListeners() {
        databaseRef.child("members")
                   .child(serverId)
                   .addChildEventListener(memberListener);

        databaseRef.child("titles")
                   .child(serverId)
                   .addValueEventListener(retrieveMemberTitles);
    }

    @Override
    protected void DestroyListeners() {
        if(memberListener != null) {
            databaseRef.child("members").child(serverId).removeEventListener(memberListener);
        }
        if(getUserDetails != null) {
            for(DatabaseReference ref : listenerHashMap.keySet()) {
                ref.removeEventListener(listenerHashMap.get(ref));
            }
        }
        if(retrieveMemberTitles != null) {
            databaseRef.child("titles").child(serverId).removeEventListener(retrieveMemberTitles);
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return R.id.membersMenuItem;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
