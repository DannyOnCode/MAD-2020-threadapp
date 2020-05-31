package com.threadteam.thread.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.threadteam.thread.R;
import com.threadteam.thread.ViewServerAdapter;
import com.threadteam.thread.models.Server;

import java.util.ArrayList;
import java.util.List;

public class ViewServersActivity extends AppCompatActivity {

    // DATA STORE
    private List<Server> serverList;
    private ViewServerAdapter adapter;

    // VIEW OBJECTS
    private RecyclerView ViewServerRecyclerView;
    private Toolbar ServerNavToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ServerNavToolbar = (Toolbar) findViewById(R.id.serverNavToolbar);
        this.setSupportActionBar(ServerNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_view_servers);

        // BIND VIEW OBJECTS
        ViewServerRecyclerView = (RecyclerView) findViewById(R.id.viewServerRecyclerView);

        serverList = getServersForUser(0);

        adapter = new ViewServerAdapter(serverList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        ViewServerRecyclerView.setLayoutManager(layoutManager);
        ViewServerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ViewServerRecyclerView.setAdapter(adapter);
    }

    private List<Server> getServersForUser(Integer userId) {
        return new ArrayList<>();
    }
}
