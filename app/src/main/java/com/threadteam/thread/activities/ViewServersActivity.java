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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private ValueEventListener subscriptionListener;

    // DATA STORE
    private List<Server> serverList = new ArrayList<>();
    private ViewServerAdapter adapter;

    // VIEW OBJECTS
    private RecyclerView ViewServerRecyclerView;
    private ActionMenuView BottomToolbarAMV;
    private Toolbar TopNavToolbar;
    private Toolbar BottomToolbar;
    private Button BottomToolbarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_servers);

        // SETUP TOOLBARS
        View topNavView = findViewById(R.id.serversNavBarInclude);
        View bottomToolbarView = findViewById(R.id.serversBottomToolbarInclude);
        TopNavToolbar = (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
        BottomToolbar = (Toolbar) bottomToolbarView.findViewById(R.id.bottomToolbar);
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

        //TODO: TEMPORARY SIGN IN PARAMETERS TO ACCESS TEST DUMMY ACC. DELETE BEFORE RELEASE!
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword("test@test.com","test123");

        // INITIALISE FIREBASE
        currentUser = firebaseAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Adds a single server to adapter if servers.{serverId} exists. Single Value Event Type.
        final ValueEventListener addServerOnce = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Log.v(LogTAG, "Server for the serverId does not exist!");
                    return;
                }

                Server server = dataSnapshot.getValue(Server.class);
                adapter.serverList.add(server);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.v(LogTAG, "DatabaseError! " + databaseError.toString());
            }
        };

        // Main Value Event Listener which runs addServerOnce for all subscribed servers on user's
        // subscribed servers changed.
        subscriptionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Reset adapter's data store to prepare for new data
                adapter.serverList.clear();

                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getKey() == null) {
                        Log.v(LogTAG, "ServerId returned null! Aborting retrieval of server details!");
                        return;
                    }

                    String serverId = data.getKey();
                    databaseRef.child("servers").child(serverId).addListenerForSingleValueEvent(addServerOnce);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.v(LogTAG, "DatabaseError! " + databaseError.toString());
            }
        };

        // Add a value event listener that only runs while this activity is still running
        databaseRef.child("users").child(currentUser.getUid())
                .child("_subscribedServers").addValueEventListener(subscriptionListener);
    }

    @Override
    protected void onStop() {
        // Destroy Value Event Listeners when this activity stops.
        databaseRef.removeEventListener(subscriptionListener);
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT);
        Log.v(LogTAG, message);
    }

    // TOOLBAR OVERRIDE METHODS

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar_menu, BottomToolbarAMV.getMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
        //TODO: handle bottom toolbar menu taps
    }

}
