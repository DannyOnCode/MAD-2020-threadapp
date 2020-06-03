package com.threadteam.thread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.models.User;

import java.util.List;

public class ViewProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    // DATA STORE
    public User userData;
    public Integer count = 0;
    public ViewProfileAdapter(User user) {
        this.userData = userData;
    }

    public int getItemViewType(int position) {
        if(position == 0){
            return position;
        }
        else if(position == 1){
            return position;
        }
        else{
            return 2;
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(viewType){
            case 0:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_profilecard,parent,false);
                return new ViewProfileCardViewHolder(view);
            case 1:
                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_server_divider,parent,false);
                return new ViewDividerViewHolder(view1);
            case 2:
                View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_serverstatuscard,parent,false);
                return new ViewServerStatusCardViewHolder(view2);
        }
        View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_profilecard,parent,false);
        return new ViewProfileCardViewHolder(view1);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch(holder.getItemViewType()) {
            case 0:
                ViewProfileCardViewHolder profileHolder = (ViewProfileCardViewHolder) holder;
                Picasso.get()
                        .load(userData.get_profileImageURL())
                        .fit()
                        .centerCrop()
                        .into(profileHolder.userImage);
                profileHolder.userName.setText(userData.get_username());
                profileHolder.titleStatus.setText(userData.get_statusMessage());
                profileHolder.aboutMeDesc.setText(userData.get_aboutUsMessage());
                break;
            case 1:
                ViewDividerViewHolder dividerHolder = (ViewDividerViewHolder) holder;
                break;
            case 2:
                Server serverDetails = userData.get_subscribedServers().get(count);
                ViewServerStatusCardViewHolder serverStatusCardViewHolder = (ViewServerStatusCardViewHolder) holder;
                serverStatusCardViewHolder.serverName.setText(serverDetails.get_name());
                serverStatusCardViewHolder.serverTitle.setText(serverDetails.get_desc());
                serverStatusCardViewHolder.serverLevel.setText("N/A");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 2 + userData.get_subscribedServers().size();
    }
}
