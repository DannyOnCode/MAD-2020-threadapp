package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.adapters.EditMemberTitleAdapter;
import com.threadteam.thread.abstracts.ServerBaseActivity;

import java.util.HashMap;
import java.util.List;

/**
 * This activity class handles the settings for a server.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 */

public class ServerSettingsActivity extends ServerBaseActivity {

    // DATA STORE

    /** Adapter object for EditMemberTitleRecyclerView. */
    private EditMemberTitleAdapter adapter;

    // VIEW OBJECTS
    //
    // EditMemberTitleRecyclerView:     DISPLAYS ALL CHAT MESSAGES IN THE SERVER. USES adapter AS ITS ADAPTER.
    // EditServerTitleEditText:         EDIT TEXT VIEW OBJECT FOR KEYING IN NEW SERVER TITLE
    // EditServerDescEditText:          EDIT TEXT VIEW OBJECT FOR KEYING IN NEW SERVER DESCRIPTION
    // EditServerDetailsApplyButton:    APPLIES CHANGES TO SERVER TITLE AND DESCRIPTION ONCE CLICKED
    // EditMemberTitlesApplyButton:     APPLIES CHANGES TO MEMBER TITLES ONCE CLICKED

    /** Allows the user to key in a new server title. */
    private EditText EditServerTitleEditText;

    /** Allows the user to key in a new server description. */
    private EditText EditServerDescEditText;

    /** Applies the new server title and description when triggered. */
    private Button EditServerDetailsApplyButton;

    /**
     * Allows the user to view and edit existing member titles for the server.
     * Uses EditMemberTitleAdapter as its adapter.
     * @see EditMemberTitleAdapter
     */

    private RecyclerView EditMemberTitleRecyclerView;

    /** Applies the new member titles (if any) when triggered. */
    private Button EditMemberTitlesApplyButton;

    // INITIALISE LISTENERS

    /**
     *  Retrieves the current server's name and description, then pre-fills it into the appropriate text fields.
     *
     *  Database Path:      root/servers/(serverId)
     *  Usage:              Single ValueEventListener
     */

    private ValueEventListener retrieveServerDetails = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String oldName = (String) dataSnapshot.child("_name").getValue();
            if (oldName == null) {
                logHandler.printDatabaseResultLog(".child(\"_name\").getValue()", "Server Name", "retrieveServerDetails", "null");
                return;
            }

            logHandler.printDatabaseResultLog(".child(\"_name\").getValue()", "Server Name", "retrieveServerDetails", oldName);
            EditServerTitleEditText.setText(oldName);

            String oldDesc = (String) dataSnapshot.child("_desc").getValue();
            if (oldDesc == null) {
                logHandler.printDatabaseResultLog(".child(\"_desc\").getValue()", "Server Desc", "retrieveServerDetails", "null");
                return;
            }

            logHandler.printDatabaseResultLog(".child(\"_desc\").getValue()", "Server Desc", "retrieveServerDetails", oldDesc);
            EditServerDescEditText.setText(oldDesc);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    /**
     *  Retrieves the current server's member titles, then pre-fills it into the appropriate text fields.
     *
     *  Database Path:      root/titles/(serverId)
     *  Usage:              Single ValueEventListener
     */

    private ValueEventListener retrieveMemberTitles = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Member Title Data", "retrieveMemberTitles", "null");
                return;
            }

            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (snapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog("snapshot.getKey()", "Member Title Index", "retrieveMemberTitles", "null");
                    return;
                }

                int index = Integer.parseInt(snapshot.getKey());
                logHandler.printDatabaseResultLog("snapshot.getKey()", "Member Title", "retrieveMemberTitles", Integer.toString(index));

                String title = (String) snapshot.getValue();
                if (title == null) {
                    logHandler.printDatabaseResultLog("snapshot.getValue()", "Member Title", "retrieveMemberTitles", "null");
                    return;
                }
                logHandler.printDatabaseResultLog("snapshot.getValue()", "Member Title", "retrieveMemberTitles", title);

                adapter.titleData.set(index, title);
                adapter.notifyItemChanged(index);
            }
        }

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
    protected ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseServerSettingsConstraintLayout);
    }

    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_server_settings;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return ServerSettingsActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "Server Settings";
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.serverSettingsNavBarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return R.id.serverSettingsBottomToolbarInclude;
    }

    @Override
    protected ImageButton setMainActionButton() {
        View bottomToolbarView = findViewById(R.id.serverSettingsBottomToolbarInclude);
        ImageButton mainActionButton = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);
        mainActionButton.setVisibility(View.GONE);
        return mainActionButton;
    }

    @Override
    protected void BindViewObjects() {
        EditServerTitleEditText = (EditText) findViewById(R.id.editServerTitleEditText);
        EditServerDescEditText = (EditText) findViewById(R.id.editServerDescEditText);
        EditServerDetailsApplyButton = (Button) findViewById(R.id.editServerDetailsApplyButton);

        EditMemberTitleRecyclerView = (RecyclerView) findViewById(R.id.editMemberTitleRecyclerView);
        EditMemberTitlesApplyButton = (Button) findViewById(R.id.editMemberTitlesApplyButton);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void SetupViewObjects() {
        adapter = new EditMemberTitleAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        EditMemberTitleRecyclerView.setLayoutManager(layoutManager);
        EditMemberTitleRecyclerView.setItemAnimator(new DefaultItemAnimator());
        EditMemberTitleRecyclerView.setAdapter(adapter);
        EditMemberTitleRecyclerView.setNestedScrollingEnabled(false);

        EditServerDetailsApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleApplyServerDetailChanges();
            }
        });

        EditMemberTitlesApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleApplyMemberTitleChanges();
            }
        });
    }

    @Override
    protected void AttachListeners() {
        databaseRef.child("servers")
                   .child(serverId)
                   .addListenerForSingleValueEvent(retrieveServerDetails);

        databaseRef.child("titles")
                   .child(serverId)
                   .addListenerForSingleValueEvent(retrieveMemberTitles);
    }

    @Override
    protected void DestroyListeners() { }

    @Override
    protected int setCurrentMenuItemID() {
        return R.id.settingsMenuItem;
    }

    // ACTIVITY SPECIFIC METHODS

    /**
     * Updates the current server's name and description to the value of their respective text fields.
     */

    private void HandleApplyServerDetailChanges() {
        String newName = EditServerTitleEditText.getText().toString().trim();
        String newDesc = EditServerDescEditText.getText().toString().trim();

        if(newName.length() == 0) {
            EditServerTitleEditText.setError("Your server name can't be empty!");
            return;
        }

        if(serverId == null) {
            logHandler.printLogWithMessage("Couldn't get serverId!");
            return;
        }

        databaseRef.child("servers")
                   .child(serverId)
                   .child("_name")
                   .setValue(newName);

        databaseRef.child("servers")
                   .child(serverId)
                   .child("_desc")
                   .setValue(newDesc);
    }

    /**
     * Updates the current server's member titles to the value of their respective text fields.
     */

    private void HandleApplyMemberTitleChanges() {
        List<String> newTitles = adapter.titleData;
        HashMap<String, String> titleListMap = new HashMap<>();

        for(int i = 0; i<newTitles.size(); i++) {
            if(!newTitles.get(i).equals("")) {
                titleListMap.put(Integer.toString(i), newTitles.get(i));
            }
        }

        databaseRef.child("titles")
                .child(serverId)
                .setValue(titleListMap);
    }
}