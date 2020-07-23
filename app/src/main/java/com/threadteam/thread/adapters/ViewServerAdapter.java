package com.threadteam.thread.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.viewholders.ViewServerViewHolder;
import com.threadteam.thread.models.Server;

import java.util.List;

// CHAT MESSAGE ADAPTER CLASS
//
// PROGRAMMER-IN-CHARGE:
// EUGENE LONG, S10193060J
//
// DESCRIPTION
// ADAPTER USED BY ViewServerRecyclerView in ViewServersActivity
// USES ViewServerViewHolder

/**
 * Handles binding server data to ViewServerViewHolder for display.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 1.0
 *
 * @see ViewServerViewHolder
 */

public class ViewServerAdapter extends RecyclerView.Adapter<ViewServerViewHolder> {

    // DATA STORE

    /** Contains all server data to be presented. */
    public List<Server> serverList;

    // CONSTRUCTOR

    public ViewServerAdapter(List<Server> servers) { this.serverList = servers; }

    @NonNull
    @Override
    public ViewServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ViewServerViewHolder viewHolder;

        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.activity_partial_view_servers,
                parent,
                false
        );

        viewHolder = new ViewServerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewServerViewHolder holder, int position) {

        String serverName = serverList.get(position).get_name();
        String serverDesc = serverList.get(position).get_desc();
        holder.ViewServerNameTextView.setText(serverName);
        holder.ViewServerDescTextView.setText(serverDesc);

        LogHandler.staticPrintLog("Binding ViewServerViewHolder with data: " + serverName + ", " + serverDesc);
    }

    @Override
    public int getItemCount() {
        return serverList.size();
    }
}
