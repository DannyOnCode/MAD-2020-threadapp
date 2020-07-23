package com.threadteam.thread.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.threadteam.thread.libraries.Progression;
import com.threadteam.thread.R;
import com.threadteam.thread.models.User;
import com.threadteam.thread.viewholders.ViewMemberDividerViewHolder;
import com.threadteam.thread.viewholders.ViewMemberViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles binding user, title and associated server data to the appropriate view holder
 * to display a list of all members in the current server.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 *
 * @see ViewMemberViewHolder
 * @see ViewMemberDividerViewHolder
 */

public class ViewMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // DATA STORE

    /** Contains all users' data to be displayed. */
    public List<User> userList;

    /** Contains custom title data for the current server. */
    public List<String> titleList = new ArrayList<>();

    /** Contains the current server id. */
    public String serverId;

    // CONSTRUCTOR

    public ViewMemberAdapter(List<User> users, String serverId) {
        this.userList = users;
        this.serverId = serverId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.activity_partial_members_divider,
                    parent,
                    false
            );

            return new ViewMemberDividerViewHolder(view);
        }

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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(position != 1) {
            ViewMemberViewHolder thisHolder = (ViewMemberViewHolder) holder;

            if(position > 1) {
                position -= 1;
            }

            User currentUser = userList.get(position);
            String nameString = currentUser.get_username();
            String imageLink = currentUser.get_profileImageURL();

            int level = currentUser.GetUserLevelForServer(serverId);
            int exp = currentUser.GetUserExpForServer(serverId);
            int expToNextLevel = currentUser.GetExpToNextLevelForServer(serverId);
            int progressToNextLevel = currentUser.GetAbsoluteLevelProgressForServer(serverId);
            int stage = Progression.ConvertLevelToStage(level);
            Integer colorInt = Progression.GetDefaultColorIntForStage(stage);

            String title;
            if(titleList.size() > 0 && !titleList.get(stage).equals("")) {
                title = titleList.get(stage);
            } else {
                title = Progression.GetDefaultTitleForStage(stage);
            }

            String levelString = "Lvl " + level;
            String expString = exp + "/" + expToNextLevel + " xp";

            thisHolder.MemberName.setText(nameString);

            if(colorInt != null) {
                thisHolder.MemberName.setTextColor(colorInt);
            }

            thisHolder.MemberTitle.setText(title);
            thisHolder.MemberLevel.setText(levelString);
            thisHolder.MemberProgressBar.setProgress(progressToNextLevel);
            thisHolder.MemberExp.setText(expString);

            if(imageLink != null) {
                Picasso.get()
                        .load(imageLink)
                        .fit()
                        .error(R.drawable.profilepictureempty)
                        .centerCrop()
                        .into(thisHolder.MemberProfileImageView);
            } else {
                Picasso.get()
                        .load(R.drawable.profilepictureempty)
                        .fit()
                        .centerCrop()
                        .into(thisHolder.MemberProfileImageView);
            }
        }
    }

    public int getItemViewType(int position) {
        if (position == 1) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        if(userList.size() == 0) {
            return 0;
        }
        return userList.size() + 1;
    }

}
