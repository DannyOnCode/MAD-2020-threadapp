package com.threadteam.thread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.models.User;

import java.util.List;

public class ViewProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    // DATA STORE
    public User userData;
    public Integer count = 0;
    DatabaseReference ref;
    final String TAG = "ViewProfile Page: ";
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
                try{
                    Picasso.get().setLoggingEnabled(true);
                    Picasso.get()
                            .load(userData.get_profileImageURL())
                            .fit()
                            .error(R.drawable.profilepictureempty)
                            .centerCrop()
                            .into(profileHolder.userImage);
                }
                catch(Exception e){
                    profileHolder.userImage.setImageResource(R.drawable.profilepictureempty);
                    Log.v(TAG,"No Profile Picture Found!");
                }
                profileHolder.userName.setText(userData.get_username());
                profileHolder.titleStatus.setText(userData.get_statusMessage());
                profileHolder.aboutMeDesc.setText(userData.get_aboutUsMessage());
                break;
            case 1:
                ViewDividerViewHolder dividerHolder = (ViewDividerViewHolder) holder;
                break;
            case 2:
                final ViewServerStatusCardViewHolder serverStatusCardViewHolder = (ViewServerStatusCardViewHolder) holder;
                String serverDetails = userData.get_subscribedServers().get(count);
                ref = FirebaseDatabase.getInstance().getReference().child("servers").child(serverDetails);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String serverName = (String) dataSnapshot.child("_name").getValue();
                        String serverTitle = (String) dataSnapshot.child("_desc").getValue();

                        serverStatusCardViewHolder.serverName.setText(serverName);
                        serverStatusCardViewHolder.serverTitle.setText(serverTitle);
                        serverStatusCardViewHolder.serverLevel.setText("N/A");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.v(TAG, "DatabaseError! " + databaseError.toString());
                    }
                });
                count += 1;
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(userData == null){
            return 0;
        }
        return 2 + userData.get_subscribedServers().size();
    }
}
