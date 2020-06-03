package com.threadteam.thread.activities;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.threadteam.thread.ViewServerAdapter;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.models.Server;

import java.util.ArrayList;
import java.util.List;

public class ViewServersActivity extends AppCompatActivity {

    private static final String LogTAG = "ThreadApp: ";

    // FIREBASE
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;
    private ChildEventListener subscriptionListener;

    // DATA STORE
    private List<Server> serverList = new ArrayList<>();
    private ViewServerAdapter adapter;
    private final int LOG_OUT_MENU_ITEM_ID = -1;

    // VIEW OBJECTS
    private RecyclerView ViewServerRecyclerView;
    private ActionMenuView BottomToolbarAMV;
    private Toolbar TopNavToolbar;
    private Button BottomToolbarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_servers);

        // SETUP TOOLBARS
        View topNavView = findViewById(R.id.serversNavBarInclude);
        View bottomToolbarView = findViewById(R.id.serversBottomToolbarInclude);
        TopNavToolbar = (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
        BottomToolbarAMV = (ActionMenuView) bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
        BottomToolbarButton = (Button) bottomToolbarView.findViewById(R.id.bottomToolbarButton);

        this.setSupportActionBar(TopNavToolbar);
        TopNavToolbar.setTitle("View Servers");
        BottomToolbarAMV.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_add_white_24);
        BottomToolbarButton.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        BottomToolbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddServer();
            }
        });

        // BIND VIEW OBJECTS
        ViewServerRecyclerView = (RecyclerView) findViewById(R.id.viewServerRecyclerView);

        adapter = new ViewServerAdapter(serverList);
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

        // INITIALISE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null) {
            Log.v(LogTAG, "User not signed in, returning to login activity!");
            Intent backToLogin = new Intent(ViewServersActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Adds a single server to adapter if servers.{serverId} exists.
        final ValueEventListener addServerOnce = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Log.v(LogTAG, "Server for the serverId does not exist! Aborting add server!");
                    return;
                }

                Server server = dataSnapshot.getValue(Server.class);

                if (server == null) {
                    Log.v(LogTAG, "Server could not be formed from dataSnapshot correctly! Aborting add server!");
                    return;
                }

                server.set_id(dataSnapshot.getKey());
                adapter.serverList.add(server);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.v(LogTAG, "DatabaseError! " + databaseError.toString());
            }
        };

        // Main Child Event Listener that calls addServerOnce as a SingleValue Event to load all servers
        // in user's subscribed servers
        subscriptionListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey() == null) {
                    Log.v(LogTAG, "ServerId returned null! Aborting retrieval of server details!");
                    return;
                }

                String newServerId = dataSnapshot.getKey();
                databaseRef.child("servers").child(newServerId).addListenerForSingleValueEvent(addServerOnce);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                for(Server server : adapter.serverList) {
                    if(server.get_id().equals(dataSnapshot.getKey())) {
                        adapter.serverList.remove(server);
                        return;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.v(LogTAG, "DatabaseError! " + databaseError.toString());
            }
        };

        // Add a child event listener that only runs while this activity is still running
        databaseRef.child("users").child(currentUser.getUid())
                .child("_subscribedServers").addChildEventListener(subscriptionListener);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onStop() {
        // Destroy Child Event Listeners when this activity stops.
        if (subscriptionListener != null) {
            databaseRef.removeEventListener(subscriptionListener);
        }

        // Reset disabled ActionMenuItemView button back to normal state
        ActionMenuItemView viewServers = (ActionMenuItemView) findViewById(R.id.viewServersMenuItem);
        viewServers.setEnabled(true);
        Drawable enabled = ContextCompat.getDrawable(this, R.drawable.round_chat_white_36);
        if(enabled == null) {
            Log.v(LogTAG, "drawable for round_chat_white_36 not found! Cancelling icon update!");
        } else {
            enabled.setColorFilter(null);
            viewServers.setIcon(enabled);
        }

        super.onStop();
    }

    // CLASS METHODS

    private void handleTransitionIntoServer(Integer position) {
        Intent transitionToChat = new Intent(ViewServersActivity.this, ChatActivity.class);
        transitionToChat.putExtra("SERVER_ID", serverList.get(position).get_id());
        startActivity(transitionToChat);
    }

    private void handleAddServer() {
        Intent transitionToAddServer = new Intent(ViewServersActivity.this, AddServerActivity.class);
        startActivity(transitionToAddServer);
    }

    private void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.v(LogTAG, message);
    }

    // TOOLBAR OVERRIDE METHODS

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Make ViewServers Button on menu bar look disabled
        ActionMenuItemView viewServers = (ActionMenuItemView) findViewById(R.id.viewServersMenuItem);
        viewServers.setEnabled(false);
        Drawable disabled = ContextCompat.getDrawable(this, R.drawable.round_chat_white_36);

        if(disabled == null) {
            Log.v(LogTAG, "drawable for round_chat_white_36 not found! Cancelling icon update!");
        } else {
            disabled.setColorFilter(Color.argb(40, 255, 255, 255), PorterDuff.Mode.MULTIPLY);
            viewServers.setIcon(disabled);
        }

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
                return true;
            case R.id.viewProfileMenuItem:
                Intent goToViewProfile = new Intent(ViewServersActivity.this, ViewProfileActivity.class);
                startActivity(goToViewProfile);
                onStop();
                return true;
            case LOG_OUT_MENU_ITEM_ID:
                firebaseAuth.signOut();
                Intent logOutToSignIn = new Intent(ViewServersActivity.this, LoginActivity.class);
                startActivity(logOutToSignIn);
                onStop();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
