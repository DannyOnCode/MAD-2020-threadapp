package com.threadteam.thread.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.threadteam.thread.R;
import com.threadteam.thread.Utils;
import com.threadteam.thread.models.User;
import com.threadteam.thread.viewholders.ViewMemberViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ViewMemberAdapter extends RecyclerView.Adapter<ViewMemberViewHolder> {

    // DATA STORE
    public List<User> userList;
    public List<String> titleList = new ArrayList<>();
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
        int stage = Utils.ConvertLevelToStage(level);
        Integer colorInt = Utils.GetDefaultColorIntForStage(stage);

        String title;
        if(titleList.size() > 0 && !titleList.get(stage).equals("")) {
            title = titleList.get(stage);
        } else {
            title = Utils.GetDefaultTitleForStage(stage);
        }

        String levelString = "Lvl " + level;
        String expString = exp + "/" + expToNextLevel + " xp";

        holder.MemberName.setText(nameString);

        if(colorInt != null) {
            holder.MemberName.setTextColor(colorInt);
        }

        holder.MemberTitle.setText(title);
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
        } else {
            holder.MemberProfileImageView.setImageResource(R.drawable.profilepictureempty);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}
