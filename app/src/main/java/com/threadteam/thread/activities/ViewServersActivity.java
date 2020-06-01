package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.threadteam.thread.R;
import com.threadteam.thread.RecyclerTouchListener;
import com.threadteam.thread.ViewServerAdapter;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.models.Server;

import java.util.ArrayList;
import java.util.List;

public class ViewServersActivity extends AppCompatActivity {

    // DATA STORE
    private List<Server> serverList = new ArrayList<>();
    private ViewServerAdapter adapter;
    private int REQUEST_CODE = 1;

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

        serverList = getServersForUser();

        // Test RecyclerView
        serverList.add(new Server(0, 0, "MAD Team 7 Discussion", "we're ded lol"));
        serverList.add(new Server(0, 0, "ICT KMS", "we're ded too lol"));
        serverList.add(new Server(0, 0, "Serial Memers Chat", "come in for fun and memes you'll be dying to read"));

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
    }

    private List<Server> getServersForUser() {
        return new ArrayList<>();
    }

    private void handleTransitionIntoServer(Integer position) {
        //TODO: Transition into chat view with serverid
        Intent transitionToChat = new Intent(ViewServersActivity.this, ChatActivity.class);
        transitionToChat.putExtra("SERVER_ID", serverList.get(position).get_id());
        startActivity(transitionToChat);
    }

    private void handleAddServer() {
        Intent transitionToAddServer = new Intent(ViewServersActivity.this, AddServerActivity.class);
        startActivityForResult(transitionToAddServer, REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar_menu, BottomToolbarAMV.getMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Disable for now since this'll override test server objects
            //adapter.serverList = getServersForUser();
            //adapter.notifyDataSetChanged();
        }
    }
}
