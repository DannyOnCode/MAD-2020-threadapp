package com.threadteam.thread.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.viewholders.ViewDividerViewHolder;
import com.threadteam.thread.viewholders.ViewProfileCardViewHolder;
import com.threadteam.thread.viewholders.ViewServerStatusCardViewHolder;
import com.threadteam.thread.models.User;

// CHAT MESSAGE ADAPTER CLASS
//
// PROGRAMMER-IN-CHARGE:
// EUGENE LONG, S10193060J
//
// DESCRIPTION
// ADAPTER USED BY profileView RECYCLERVIEW in Profile Activity
// USES ViewProfileCardView, ViewDividerViewHolder, ViewServerStatusCardViewHolder

public class ViewProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // LOGGING
    private LogHandler logHandler = new LogHandler("ViewProfile Adaptor");

    // DATA STORE
    //
    // mContext:        CONTAINS CONTEXT FROM VIEW PROFILE ACTIVITY
    // userData:        CONTAINS USER OBJECT FROM PROFILE ACTIVITY
    private Context mContext;
    public User userData;

    // FIREBASE
    //
    // databaseRef:     FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION.
    DatabaseReference ref;

    public ViewProfileAdapter(User user) {

        this.userData = userData;
    }

    //Get View Type for each position
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
    //Inflate RecyclerView with viewholder
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
            case 2:
                View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_serverstatuscard,parent,false);
                return new ViewServerStatusCardViewHolder(view2);
        }
        View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_profile,parent,false);
        return new ViewProfileCardViewHolder(view1);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch(holder.getItemViewType()) {

            case 0:
                //Profile Card
                ViewProfileCardViewHolder profileHolder = (ViewProfileCardViewHolder) holder;
                Picasso.get().setLoggingEnabled(true);
                if(userData.get_profileImageURL() != null){
                    Picasso.get()
                            .load(userData.get_profileImageURL())
                            .fit()
                            .error(R.drawable.profilepictureempty)
                            .centerCrop()
                            .into(profileHolder.userImage);
                    }
                else{
                    profileHolder.userImage.setImageResource(R.drawable.profilepictureempty);
                    logHandler.printLogWithMessage("No Profile Image found");
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
                final ViewServerStatusCardViewHolder serverStatusCardViewHolder = (ViewServerStatusCardViewHolder) holder;
                String serverDetails = userData.get_subscribedServers().get(position - 2);
                ref = FirebaseDatabase.getInstance().getReference().child("servers").child(serverDetails);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String serverName = (String) dataSnapshot.child("_name").getValue();
                        String serverTitle = (String) dataSnapshot.child("_desc").getValue();

                        serverStatusCardViewHolder.serverName.setText(serverName);
                        serverStatusCardViewHolder.serverTitle.setText(serverTitle);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        logHandler.printDatabaseErrorLog(databaseError);
                    }
                });
                Integer serverLevelData = userData.GetUserLevelForServer(serverDetails);
                serverStatusCardViewHolder.serverLevel.setText(serverLevelData.toString());

                serverStatusCardViewHolder.profileXpProgressBar.setProgress(userData.GetProgressToNextLevelForServer(serverDetails));

                break;
        }
    }

    @Override
    public int getItemCount() {
        if(userData == null){
            return 0;
        }
        //2 for profile card and divider ViewHolder
        return 2 + userData.get_subscribedServers().size();
    }
}