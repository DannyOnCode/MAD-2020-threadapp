package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.ViewProfileAdapter;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ViewProfileActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference ref;
    private RecyclerView profileView;

    private ValueEventListener userDataListener;

    private Toolbar TopNavToolbar;
    private ActionMenuView BottomToolbarAMV;
    private Button BottomToolbarButton;
    private final int LOG_OUT_MENU_ITEM_ID = -1;


    final String LogTAG = "ThreadApp: ";
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        View topNavView = findViewById(R.id.profileNavBarInclude);
        View bottomToolbarView = findViewById(R.id.profileBottomToolbarInclude);
        TopNavToolbar = (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
        BottomToolbarAMV = (ActionMenuView) bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
        BottomToolbarButton = (Button) bottomToolbarView.findViewById(R.id.bottomToolbarButton);

        this.setSupportActionBar(TopNavToolbar);
        TopNavToolbar.setTitle("View Profile");
        BottomToolbarAMV.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_create_white_24);
        BottomToolbarButton.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        BottomToolbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToEditProfile = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
                startActivity(goToEditProfile);
            }
        });

        profileView = (RecyclerView) findViewById(R.id.viewProfileRecyclerView);

        final ViewProfileAdapter profileAdapter = new ViewProfileAdapter(null);
        LinearLayoutManager pLayoutManager = new LinearLayoutManager(mContext);
        profileView.setLayoutManager(pLayoutManager);
        profileView.setAdapter(profileAdapter);
        profileView.setItemAnimator(new DefaultItemAnimator());


        //Getting UserData
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();


        String userID = currentUser.getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User();
                String userName = (String) dataSnapshot.child("_username").getValue();
                String profileImage = (String) dataSnapshot.child("_profileImageURL").getValue();
                String aboutMeMessages = (String) dataSnapshot.child("_aboutUsMessage").getValue();
                String statusDescription = (String) dataSnapshot.child("_statusMessage").getValue();

                user.set_username(userName);
                user.set_profileImageURL(profileImage);
                user.set_aboutUsMessage(aboutMeMessages);
                user.set_statusMessage(statusDescription);

                List<String> serverList = new ArrayList<>();
                for(DataSnapshot data : dataSnapshot.child("_subscribedServers").getChildren()){
                    serverList.add(data.getKey());
                }
                Log.v(LogTAG, Arrays.toString(serverList.toArray()));
                user.set_subscribedServers(serverList);

                profileAdapter.userData = user;
                profileAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(LogTAG,"The read failed: " + databaseError.getCode());
            }
        };

        ref.addValueEventListener(userDataListener);

    }

    @Override
    protected void onStop() {
        ref.removeEventListener(userDataListener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        toggleOwnMenuItemDisplay(true);
        super.onDestroy();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Make ViewServers Button on menu bar look disabled
        toggleOwnMenuItemDisplay(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_app_bar_menu, BottomToolbarAMV.getMenu());
        TopNavToolbar.getMenu().add(Menu.NONE, LOG_OUT_MENU_ITEM_ID, Menu.NONE, "Log Out");
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.viewServersMenuItem:

                Intent goToViewServer = new Intent(ViewProfileActivity.this, ViewServersActivity.class);
                goToViewServer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(goToViewServer);

                // Reset disabled ActionMenuItemView button back to normal state
                toggleOwnMenuItemDisplay(true);

                ViewProfileActivity.this.finish();
                return true;
            case R.id.viewProfileMenuItem:
                // DISABLED
                return true;
            case LOG_OUT_MENU_ITEM_ID:
                firebaseAuth.signOut();
                Intent logOutToSignIn = new Intent(ViewProfileActivity.this, LoginActivity.class);
                startActivity(logOutToSignIn);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    private void toggleOwnMenuItemDisplay(boolean isEnabled) {
        // Make ViewServers Button on menu bar look disabled
        ActionMenuItemView viewProfile = (ActionMenuItemView) findViewById(R.id.viewProfileMenuItem);

        if(viewProfile == null) {
            return;
        }

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.round_face_white_36);

        if(drawable == null) {
            Log.v(LogTAG, "drawable for round_chat_white_36 not found! Cancelling icon update!");
        } else {
            if(isEnabled) {
                drawable.setColorFilter(null);
            } else {
                drawable.setColorFilter(Color.argb(40, 255, 255, 255), PorterDuff.Mode.MULTIPLY);
            }
            viewProfile.setIcon(drawable);
        }
    }


}