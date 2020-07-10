package com.threadteam.thread.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.threadteam.thread.R;
import com.threadteam.thread.models.User;
import com.threadteam.thread.viewholders.ViewMemberViewHolder;

import java.util.List;

public class ViewMemberAdapter extends RecyclerView.Adapter<ViewMemberViewHolder> {

    // DATA STORE
    public List<User> userList;
    public String serverId;

    // CONSTRUCTOR
    public ViewMemberAdapter(List<User> users, String serverId) {
        this.userList = users;
        this.serverId = serverId;
    }

    @NonNull
    @Override
    public ViewMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ViewMemberViewHolder viewHolder;

        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.activity_partial_view_member,
                parent,
                false
        );

        viewHolder = new ViewMemberViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewMemberViewHolder holder, int position) {
        User currentUser = userList.get(position);
        String nameString = currentUser.get_username();
        String imageLink = currentUser.get_profileImageURL();

        int level = currentUser.GetUserLevelForServer(serverId);
        int exp = currentUser.GetUserExpForServer(serverId);
        int expToNextLevel = currentUser.GetExpToNextLevelForServer(serverId);
        int progressToNextLevel = currentUser.GetAbsoluteLevelProgressForServer(serverId);

        String levelString = "Lvl " + level;
        String expString = exp + "/" + expToNextLevel + " xp";

        holder.MemberName.setText(nameString);
        holder.MemberLevel.setText(levelString);
        holder.MemberProgressBar.setProgress(progressToNextLevel);
        holder.MemberExp.setText(expString);

        if(imageLink != null) {
            Picasso.get()
                    .load(imageLink)
                    .fit()
                    .error(R.drawable.profilepictureempty)
                    .centerCrop()
                    .into(holder.MemberProfileImageView);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}
