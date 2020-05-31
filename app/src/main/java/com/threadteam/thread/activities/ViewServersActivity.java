package com.threadteam.thread.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.threadteam.thread.R;
import com.threadteam.thread.ViewServerAdapter;
import com.threadteam.thread.models.Server;

import java.util.ArrayList;
import java.util.List;

public class ViewServersActivity extends AppCompatActivity {

    // DATA STORE
    private List<Server> serverList = new ArrayList<>();
    private ViewServerAdapter adapter;

    // VIEW OBJECTS
    private RecyclerView ViewServerRecyclerView;
    private ActionMenuView BottomToolbarAMV;
    private Toolbar TopNavToolbar;
    private Toolbar BottomToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_servers);

        TopNavToolbar = (Toolbar) findViewById(R.id.topNavToolbar);
        BottomToolbar = (Toolbar) findViewById(R.id.bottomToolbar);
        BottomToolbarAMV = (ActionMenuView) findViewById(R.id.bottomToolbarAMV);

        this.setSupportActionBar(TopNavToolbar);
        BottomToolbarAMV.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        // BIND VIEW OBJECTS
        ViewServerRecyclerView = (RecyclerView) findViewById(R.id.viewServerRecyclerView);

        serverList = getServersForUser(0);

        // Test RecyclerView
        serverList.add(new Server(0, 0, "MAD Team 7 Discussion", "we're ded lol"));
        serverList.add(new Server(0, 0, "ICT KMS", "we're ded too lol"));
        serverList.add(new Server(0, 0, "Serial Memers Chat", "come in for fun and memes you'll be dying to read"));

        adapter = new ViewServerAdapter(serverList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ViewServerRecyclerView.setLayoutManager(layoutManager);
        ViewServerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ViewServerRecyclerView.setAdapter(adapter);
    }

    private List<Server> getServersForUser(Integer userId) {
        return new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar_menu, BottomToolbarAMV.getMenu());
        return true;
    }
}
