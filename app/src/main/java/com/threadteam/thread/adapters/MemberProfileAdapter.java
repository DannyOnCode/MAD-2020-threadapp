package com.threadteam.thread.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.threadteam.thread.R;
import com.threadteam.thread.models.Post;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.models.User;
import com.threadteam.thread.viewholders.ViewDividerViewHolder;
import com.threadteam.thread.viewholders.ViewProfileCardViewHolder;
import com.threadteam.thread.viewholders.ViewServerStatusCardViewHolder;

import java.util.List;

public class MemberProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // DATA STORE
    public User userData;
    public List<Server> serverList;

    // CONSTRUCTOR
    public MemberProfileAdapter(User user, List<Server> servers) {
        this.userData = user;
        this.serverList = servers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(viewType){
            case 0:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_profile,parent,false);
                return new ViewProfileCardViewHolder(view);
            case 1:
                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_server_divider,parent,false);
                return new ViewDividerViewHolder(view1);
            default:
                View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_serverstatuscard,parent,false);
                return new ViewServerStatusCardViewHolder(view2);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch(holder.getItemViewType()) {

            case 0:
                //Profile Card
                ViewProfileCardViewHolder profileHolder = (ViewProfileCardViewHolder) holder;
                if(userData.get_profileImageURL() != null) {
                    Picasso.get().setLoggingEnabled(true);
                    Picasso.get()
                            .load(userData.get_profileImageURL())
                            .fit()
                            .error(R.drawable.profilepictureempty)
                            .centerCrop()
                            .into(profileHolder.userImage);
                }
                else {
                    profileHolder.userImage.setImageResource(R.drawable.profilepictureempty);
                }

                profileHolder.userName.setText(userData.get_username());
                profileHolder.titleStatus.setText(userData.get_statusMessage());
                profileHolder.aboutMeDesc.setText(userData.get_aboutUsMessage());
                break;
            case 1:
                //Divider
                ViewDividerViewHolder dividerHolder = (ViewDividerViewHolder) holder;
                break;
            case 2:
                //Server Card
                ViewServerStatusCardViewHolder serverStatusCardViewHolder = (ViewServerStatusCardViewHolder) holder;
                Server server = serverList.get(position - 2);

                serverStatusCardViewHolder.serverName.setText(server.get_name());
                serverStatusCardViewHolder.serverTitle.setText(server.get_desc());
                Integer serverLevelData = userData.GetUserLevelForServer(server.get_id());
                serverStatusCardViewHolder.serverLevel.setText(serverLevelData.toString());

                serverStatusCardViewHolder.profileXpProgressBar.setProgress(userData.GetProgressToNextLevelForServer(server.get_id()));

                break;
        }
    }

    public int getItemViewType(int position) {
        switch (position) {
            case 0:
            case 1:
                return position;
            default:
                return 2;
        }
    }

    @Override
    public int getItemCount() {
        if(userData == null) {
            return 0;
        }
        return 2 + serverList.size();
    }


}
