package com.threadteam.thread;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.models.Server;

import java.util.List;

public class ViewServerAdapter extends RecyclerView.Adapter<ViewServerViewHolder> {

    // DATA STORE
    List<Server> serverList;

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
        holder.ViewServerNameTextView.setText(serverList.get(position).get_name());
        holder.ViewServerDescTextView.setText(serverList.get(position).get_desc());
    }

    @Override
    public int getItemCount() {
        return serverList.size();
    }
}
