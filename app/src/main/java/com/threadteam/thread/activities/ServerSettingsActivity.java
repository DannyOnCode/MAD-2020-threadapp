package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.adapters.EditMemberTitleAdapter;

import java.util.HashMap;
import java.util.List;

public class ServerSettingsActivity extends _ServerBaseActivity {

    // DATA STORE
    //
    // adapter:                     ADAPTER FOR EDIT MESSAGE TITLES

    private EditMemberTitleAdapter adapter;

    // VIEW OBJECTS
    //
    // EditMemberTitleRecyclerView:     DISPLAYS ALL CHAT MESSAGES IN THE SERVER. USES adapter AS ITS ADAPTER.
    // EditServerTitleEditText:         EDIT TEXT VIEW OBJECT FOR KEYING IN NEW SERVER TITLE
    // EditServerDescEditText:          EDIT TEXT VIEW OBJECT FOR KEYING IN NEW SERVER DESCRIPTION
    // EditServerDetailsApplyButton:    APPLIES CHANGES TO SERVER TITLE AND DESCRIPTION ONCE CLICKED
    // EditMemberTitlesApplyButton:     APPLIES CHANGES TO MEMBER TITLES ONCE CLICKED

    private EditText EditServerTitleEditText;
    private EditText EditServerDescEditText;
    private Button EditServerDetailsApplyButton;

    private RecyclerView EditMemberTitleRecyclerView;
    private Button EditMemberTitlesApplyButton;

    // INITIALISE LISTENERS

    // retrieveServerDetails:       RETRIEVES CURRENT SERVER'S NAME AND DESCRIPTION AND PRE-FILLS IT INTO
    //                              THE APPROPRIATE EDIT TEXT OBJECTS AS
    //                              CORRECT INVOCATION CODE: databaseRef.child("servers")
    //                                                                  .child(serverId)
    //                                                                  .addListenerForSingleValueEvent(retrieveServerDetails)

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

    // retrieveMemberTitles:        RETRIEVES MEMBER TITLES AND PRE-FILLS IT INTO
    //                              THE APPROPRIATE EDIT TEXT OBJECTS AS
    //                              CORRECT INVOCATION CODE: databaseRef.child("titles")
    //                                                                  .child(serverId)
    //                                                                  .addListenerForSingleValueEvent(retrieveMemberTitles)

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
    ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseServerSettingsConstraintLayout);
    }

    @Override
    int setLayoutIDForContentView() {
        return R.layout.activity_server_settings;
    }

    @Override
    AppCompatActivity setCurrentActivity() {
        return ServerSettingsActivity.this;
    }

    @Override
    String setTitleForActivity() {
        return "Server Settings";
    }

    @Override
    Integer setTopNavToolbarIncludeId() {
        return R.id.serverSettingsNavBarInclude;
    }

    @Override
    Integer setBottomToolbarAMVIncludeId() {
        return R.id.serverSettingsBottomToolbarInclude;
    }

    @Override
    ImageButton setMainActionButton() {
        View bottomToolbarView = findViewById(R.id.serverSettingsBottomToolbarInclude);
        ImageButton mainActionButton = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);
        mainActionButton.setVisibility(View.GONE);
        return mainActionButton;
    }

    @Override
    void BindViewObjects() {
        EditServerTitleEditText = (EditText) findViewById(R.id.editServerTitleEditText);
        EditServerDescEditText = (EditText) findViewById(R.id.editServerDescEditText);
        EditServerDetailsApplyButton = (Button) findViewById(R.id.editServerDetailsApplyButton);

        EditMemberTitleRecyclerView = (RecyclerView) findViewById(R.id.editMemberTitleRecyclerView);
        EditMemberTitlesApplyButton = (Button) findViewById(R.id.editMemberTitlesApplyButton);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    void SetupViewObjects() {
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
    void AttachListeners() {
        databaseRef.child("servers")
                   .child(serverId)
                   .addListenerForSingleValueEvent(retrieveServerDetails);

        databaseRef.child("titles")
                   .child(serverId)
                   .addListenerForSingleValueEvent(retrieveMemberTitles);
    }

    @Override
    void DestroyListeners() { }

    @Override
    int setCurrentMenuItemID() {
        return R.id.settingsMenuItem;
    }

    // ACTIVITY SPECIFIC METHODS

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