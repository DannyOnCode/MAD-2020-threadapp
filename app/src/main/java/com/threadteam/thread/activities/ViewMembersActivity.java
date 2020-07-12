package com.threadteam.thread.activities;

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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.RecyclerTouchListener;
import com.threadteam.thread.adapters.ViewMemberAdapter;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.models.User;

import java.util.ArrayList;
import java.util.List;

public class ViewMembersActivity extends ServerBaseActivityTemp {

    // DATA STORE
    //
    // adapter:                 ADAPTER FOR VIEW MEMBER RECYCLER VIEW.

    private ViewMemberAdapter adapter;

    // VIEW OBJECTS
    //
    // ViewMembersRecyclerView: DISPLAYS ALL MEMBERS IN THE SERVER. USES adapter AS ITS ADAPTER.

    private RecyclerView ViewMembersRecyclerView;

    // INITIALISE LISTENERS

    // getUserListener: GETS AND ADDS A SINGLE USER TO adapter IF USER DOESN'T EXIST, ELSE UPDATES IT.
    //                  CORRECT INVOCATION CODE: databaseRef.child("users")
    //                                                      .child(userID)
    //                                                      .addValueEventListener(getUserListener)
    //                  SHOULD NOT BE USED INDEPENDENTLY.
    //                  index SHOULD BE MODIFIED BEFOREHAND TO INDICATE THE INDEX AT WHICH THE USER IS ADDED IN THE ADAPTER.

    private ValueEventListener getUserListener = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "User Values", "addUserOnce", "null");
                return;
            }

            String id = (String) dataSnapshot.getKey();
            String username = (String) dataSnapshot.child("_username").getValue();
            String profileImageURL = (String) dataSnapshot.child("_profileImageURL").getValue();

            if(id == null) {
                logHandler.printDatabaseResultLog(".getKey()", "User ID", "addUserOnce", "null");
                return;
            }
            else if(username == null) {
                logHandler.printDatabaseResultLog(".child(\"_username\").getValue()", "Username", "addUserOnce", "null");
                return;
            }
            else if(profileImageURL == null) {
                logHandler.printDatabaseResultLog(".child(\"_profileImageURL\").getValue()", "Profile Image URL", "addUserOnce", "null");
                return;
            }

            logHandler.printDatabaseResultLog(".getKey()", "User ID", "addUserOnce", id);
            logHandler.printDatabaseResultLog(".child(\"_username\").getValue()", "Username", "addUserOnce", username);
            logHandler.printDatabaseResultLog(".child(\"_profileImageURL\").getValue()", "Profile Image URL", "addUserOnce", profileImageURL);


            List<String> servers = new ArrayList<>();
            List<Integer> expList = new ArrayList<>();

            for(DataSnapshot snapshot : dataSnapshot.child("_subscribedServers").getChildren()) {

                if(snapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog(
                            ".child(\"_subscribedServers\").getChildren().getKey()",
                            "Server ID",
                            "addUserOnce",
                            "null");
                    break;
                }
                logHandler.printDatabaseResultLog(
                        ".child(\"_subscribedServers\").getChildren().getKey()",
                        "Server ID",
                        "addUserOnce",
                        snapshot.getKey());

                if(snapshot.getValue() == null) {
                    logHandler.printDatabaseResultLog(
                            ".child(\"_subscribedServers\").getChildren().getValue()",
                            "Server EXP",
                            "addUserOnce",
                            "null");
                    break;
                }
                logHandler.printDatabaseResultLog(
                        ".child(\"_subscribedServers\").getChildren().getKey()",
                        "Server ID",
                        "addUserOnce",
                        ((Long) snapshot.getValue()).toString());

                servers.add(snapshot.getKey());
                expList.add(((Long) snapshot.getValue()).intValue());
            }

            User newUser = new User(id, username, profileImageURL, "", "", servers, expList);

            for(int i=0; i<adapter.userList.size(); i++) {
                if(adapter.userList.get(i).get_id().equals(newUser.get_id())) {
                    adapter.userList.remove(i);
                    adapter.userList.add(i, newUser);
                    adapter.notifyItemChanged(i);
                    return;
                }
            }

            adapter.userList.add(adapter.userList.size(), newUser);
            adapter.notifyItemInserted(adapter.userList.size());
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
                    .addValueEventListener(getUserListener);
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
    int setLayoutIDForContentView() {
        return R.layout.activity_view_members;
    }

    @Override
    AppCompatActivity setCurrentActivity() {
        return ViewMembersActivity.this;
    }

    @Override
    String setTitleForActivity() {
        return "View Members";
    }

    @Override
    ImageButton setMainActionButton() {
        View bottomToolbarView = findViewById(R.id.viewMembersBottomToolbarInclude);
        ImageButton mainActionButton = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);
        mainActionButton.setVisibility(View.GONE);
        return mainActionButton;
    }

    @Override
    Toolbar setTopNavToolbar() {
        View topNavView = findViewById(R.id.viewMembersNavBarInclude);
        return (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
    }

    @Override
    ActionMenuView setBottomToolbarAMV() {
        View bottomToolbarView = findViewById(R.id.viewMembersBottomToolbarInclude);
        return (ActionMenuView) bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
    }

    @Override
    void BindViewObjects() {
        ViewMembersRecyclerView = (RecyclerView) findViewById(R.id.viewMembersRecyclerView);
    }

    @Override
    ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseViewMembersConstraintLayout);
    }

    @Override
    void SetupViewObjects() {
        adapter = new ViewMemberAdapter(new ArrayList<User>(), "");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ViewMembersRecyclerView.setLayoutManager(layoutManager);
        ViewMembersRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ViewMembersRecyclerView.setAdapter(adapter);

        ViewMembersRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(this, ViewMembersRecyclerView, new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        // TODO: Handle touch into member profile here
                    }
                })
        );
    }

    @Override
    void DoAdditionalSetupForFirebase() {
        adapter.serverId = serverId;
    }

    @Override
    void AttachListeners() {
        databaseRef.child("members")
                .child(serverId)
                .addChildEventListener(memberListener);
    }

    @Override
    void DestroyListeners() {
        if(memberListener != null) {
            databaseRef.removeEventListener(memberListener);
        }
        if(getUserListener != null) {
            databaseRef.removeEventListener(getUserListener);
        }
    }

    @Override
    int setCurrentMenuItemID() {
        return R.id.membersMenuItem;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
