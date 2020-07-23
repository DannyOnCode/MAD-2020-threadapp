package com.threadteam.thread.activities;

import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.abstracts.BaseActivity;
import com.threadteam.thread.abstracts.MainBaseActivity;
import com.threadteam.thread.libraries.Notifications;

// VIEW PROFILE ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// MOHAMED THABITH, S10196396BB
//
// DESCRIPTION
// Handles settings for notifications
//
// NAVIGATION
// PARENT: SETTINGS
// CHILDREN: NONE
// OTHER: NONE


public class SetNotificationsActivity extends MainBaseActivity {

    // FIREBASE
    //
    // fAuth:                FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION.
    // reff:                 FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION.
    DatabaseReference reff;
    private FirebaseAuth fAuth;


    //VIEW OBJECTS
    //
    // mMsgSwitch:          SWITCHES ON NOTIFICATIONS FOR MESSAGES
    // mSystemSwitch:       SWITCHES ON NOTIFICATIONS FOR SYSTEM
    // mPostSwitch:         SWITCHES ON NOTIFICATIONS FOR POSTS

    Switch mMsgSwitch, mSystemSwitch, mPostSwitch;

    ValueEventListener getNotificationSettings = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Notifications Values", "getNotificationSettings", "null");
                return;
            }

            String messageNotification = (String) dataSnapshot.child("_msg").getValue();

            String systemNotification = (String) dataSnapshot.child("_system").getValue();

            String  postNotification = (String) dataSnapshot.child("_post").getValue();

            if(messageNotification.equals("on")){
                mMsgSwitch.setChecked(true);
            }
            else{
                mMsgSwitch.setChecked(false);
            }

            if(systemNotification.equals("on")){
                mSystemSwitch.setChecked(true);
            }
            else{
                mSystemSwitch.setChecked(false);
            }

            if(postNotification.equals("on")){
                mPostSwitch.setChecked(true);
            }
            else{
                mPostSwitch.setChecked(false);
            }


        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };



    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_notifications;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return SetNotificationsActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "Notifications";
    }

    @Override
    protected ImageButton setMainActionButton() {
        return null;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.notificationsNavbarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return R.id.notificationBottomToolbarInclude;
    }

    @Override
    protected void BindViewObjects() {
        mMsgSwitch = (Switch) findViewById(R.id.incomingMsgSwitch);
        mSystemSwitch = (Switch) findViewById(R.id.systemSwitch);
        mPostSwitch = (Switch) findViewById(R.id.postsSwitch);

        fAuth = FirebaseAuth.getInstance();
        reff = FirebaseDatabase.getInstance().getReference().child("users");

    }

    @Override
    protected void SetupViewObjects() {
        mMsgSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mMsgSwitch.isChecked()){
                    Notifications.subscribeMsgNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_msg").setValue("on");
                    logHandler.printLogWithMessage("User turned on message notifications");



                }
                else{
                    Notifications.unsubscribeMsgNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_msg").setValue("off");
                    logHandler.printLogWithMessage("User turned off message notifications");
                }
            }
        });

        mSystemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mSystemSwitch.isChecked()){
                    Notifications.subscribeSystemNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_system").setValue("on");
                    logHandler.printLogWithMessage("User turned on system notifications");

                }
                else{
                    Notifications.unsubscribeSystemNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_system").setValue("off");
                    logHandler.printLogWithMessage("User turned off system notifications");
                }
            }
        });

        mPostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mPostSwitch.isChecked()){
                    Notifications.subscribePostsNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_post").setValue("on");
                    logHandler.printLogWithMessage("User turned on post notifications");

                }
                else{
                    Notifications.unsubscribePostsNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_post").setValue("off");
                    logHandler.printLogWithMessage("User turned off post notifications");
                }

            }
        });
    }

    @Override
    protected void AttachListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .child("_notifications")
                .addValueEventListener(getNotificationSettings);
    }

    @Override
    protected void DestroyListeners() {
        if(getNotificationSettings != null) {
            databaseRef.removeEventListener(getNotificationSettings);
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }
}
