package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.RecyclerTouchListener;
import com.threadteam.thread.Utils;
import com.threadteam.thread.adapters.ViewMemberAdapter;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewMembersActivity extends ServerBaseActivity {

    // LOGGING
    private LogHandler logHandler = new LogHandler("View Members Activity");

    // FIREBASE
    //
    // currentUser:             CURRENT USER FOR THE CURRENT SESSION
    // firebaseAuth:            FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION
    // databaseRef:             FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION
    // memberListener:          TODO: document this var
    // getUserListener:         TODO: document this var

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;
    private ChildEventListener memberListener;
    private ValueEventListener getUserListener;

    // DATA STORE
    //
    // serverId:                CONTAINS CURRENT SERVER ID DATA
    // adapter:                 ADAPTER FOR VIEW MEMBER RECYCLER VIEW.

    private String serverId;
    private ViewMemberAdapter adapter;

    // VIEW OBJECTS
    //
    // ViewMembersRecyclerView: DISPLAYS ALL MEMBERS IN THE SERVER. USES adapter AS ITS ADAPTER.
    // TopNavToolbar:           TOOLBAR OBJECT THAT HANDLES UPWARDS NAVIGATION AND THE TITLE
    // BottomToolbarAMV:        HANDLES THE MENU FOR THE BOTTOM TOOLBAR.
    // MainActionFAB:           BOTTOM TOOLBAR MAIN ACTION BUTTON. USED TO HANDLE MAIN ACTIONS ON THIS
    //                          ACTIVITY. TODO: document this var

    private RecyclerView ViewMembersRecyclerView;
    private ActionMenuView BottomToolbarAMV;
    private Toolbar TopNavToolbar;
    private ImageButton MainActionFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_members);

        // BIND TOOLBARS
        // NOTE:    IT IS IMPORTANT TO GET THE INCLUDE VIEWS BEFORE DOING FIND VIEW BY ID.
        //          THIS ENSURES THAT ANDROID CAN ALWAYS FIND THE CORRECT VIEW OBJECT.

        View topNavView = findViewById(R.id.viewMembersNavBarInclude);
        View bottomToolbarView = findViewById(R.id.viewMembersBottomToolbarInclude);
        TopNavToolbar = (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
        BottomToolbarAMV = (ActionMenuView) bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
        MainActionFAB = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);

        // SETUP TOOLBARS
        TopNavToolbar.setTitle("Members");
        this.setSupportActionBar(TopNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BottomToolbarAMV.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        MainActionFAB.setVisibility(View.GONE);

        // BIND VIEW OBJECTS
        ViewMembersRecyclerView = (RecyclerView) findViewById(R.id.viewMembersRecyclerView);

        // SETUP VIEW OBJECTS
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

        // Get serverId from Intent
        final Intent dataReceiver = getIntent();
        serverId = dataReceiver.getStringExtra("SERVER_ID");

        if (serverId == null) {
            logHandler.printGetExtrasResultLog("SERVER_ID", "null");
        }
        logHandler.printGetExtrasResultLog("SERVER_ID", serverId);

        // Update adapter
        adapter.serverId = serverId;

        // INITIALISE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null) {
            logHandler.printDefaultLog(LogHandler.FIREBASE_USER_NOT_FOUND);
            Intent backToLogin = new Intent(ViewMembersActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference();

        // INITIALISE LISTENERS

        // getUserListener: GETS AND ADDS A SINGLE USER TO adapter IF USER DOESN'T EXIST, ELSE UPDATES IT.
        //                  CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                      .child(userID)
        //                                                      .addValueEventListener(getUserListener)
        //                  SHOULD NOT BE USED INDEPENDENTLY.
        //                  index SHOULD BE MODIFIED BEFOREHAND TO INDICATE THE INDEX AT WHICH THE USER IS ADDED IN THE ADAPTER.

        getUserListener = new ValueEventListener() {
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

        memberListener = new ChildEventListener() {
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

        databaseRef.child("members")
                   .child(serverId)
                   .addChildEventListener(memberListener);
    }

    @Override
    protected void onDestroy() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_DESTROY);

        // CANCEL CHILD EVENT LISTENERS ON ACTIVITY DESTROYED
        if (memberListener != null) {
            databaseRef.removeEventListener(memberListener);
        }

        if (getUserListener != null) {
            databaseRef.removeEventListener(getUserListener);
        }

        // RESET VIEW SERVERS MENU ITEM
        Utils.ToggleMenuItemAlpha(
                this,
                R.id.membersMenuItem,
                "View Members",
                R.drawable.round_group_white_36,
                "round_group_white_36",
                true,
                logHandler
        );

        super.onDestroy();
    }

    // CLASS METHODS

    // TOOLBAR OVERRIDE METHODS

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Make ViewServers Button on menu bar look disabled
        Utils.ToggleMenuItemAlpha(
                this,
                R.id.membersMenuItem,
                "View Members",
                R.drawable.round_group_white_36,
                "round_group_white_36",
                false,
                logHandler
        );
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        addServerMenuItemsToMenu(menu);
        getMenuInflater().inflate(R.menu.server_menu, BottomToolbarAMV.getMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        HashMap<String, String> extraMap = new HashMap<String, String>();

        switch (item.getItemId()) {
            case R.id.postsMenuItem:
                logHandler.printLogWithMessage("User tapped on Posts Menu Item!");

                extraMap.put("SERVER_ID", serverId);
                Utils.StartActivityOnNewStack(
                        ViewMembersActivity.this,
                        PostsActivity.class,
                        "Posts Activity",
                        extraMap,
                        logHandler);

                // Reset disabled ActionMenuItemView button back to normal state
                Utils.ToggleMenuItemAlpha(
                        this,
                        R.id.membersMenuItem,
                        "View Members",
                        R.drawable.round_group_white_36,
                        "round_group_white_36",
                        true,
                        logHandler
                );

                finish();
                return false;

            case R.id.chatMenuItem:
                logHandler.printLogWithMessage("User tapped on Chat Menu Item!");

                extraMap = new HashMap<String, String>();
                extraMap.put("SERVER_ID", serverId);
                Utils.StartActivityOnNewStack(
                        ViewMembersActivity.this,
                        ChatActivity.class,
                        "Chat Activity",
                        extraMap,
                        logHandler);

                // Reset disabled ActionMenuItemView button back to normal state
                Utils.ToggleMenuItemAlpha(
                        this,
                        R.id.membersMenuItem,
                        "View Members",
                        R.drawable.round_group_white_36,
                        "round_group_white_36",
                        true,
                        logHandler
                );

                finish();
                return true;

            case R.id.membersMenuItem:
                // DISABLED
                return false;

            case android.R.id.home:
                logHandler.printLogWithMessage("User tapped on Back Button!");

                Utils.StartActivityOnNewStack(
                        ViewMembersActivity.this,
                        ViewServersActivity.class,
                        "View Servers Activity",
                        null,
                        logHandler);
                break;

            case SHARE_SERVER_MENU_ITEM:
                logHandler.printLogWithMessage("User tapped on Share Server Menu Item!");
                ConstraintLayout baseLayer = (ConstraintLayout) findViewById(R.id.baseViewMembersConstraintLayout);
                showShareServerPopup(baseLayer, serverId);
                break;

            case LEAVE_SERVER_MENU_ITEM:
                logHandler.printLogWithMessage("User tapped on Leave Server Menu Item!");
                handleLeaveServerAlert(ViewMembersActivity.this, serverId, currentUser.getUid());
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}