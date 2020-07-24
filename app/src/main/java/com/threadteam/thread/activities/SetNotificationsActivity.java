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
import com.threadteam.thread.abstracts.MainBaseActivity;
import com.threadteam.thread.libraries.Notifications;

/**
 * This activity class handles the notification settings of user.
 *
 * @author Mohamed Thabith
 * @version 2.0
 * @since 2.0
 */


public class SetNotificationsActivity extends MainBaseActivity {

    // FIREBASE
    /** Firebase Authentication instance for the current session. */
    /** Firebase Database Reference for the current session. */
    DatabaseReference reff;
    private FirebaseAuth fAuth;


    //VIEW OBJECTS
    /** Switch
     *
     * mMsgSwitch       Switch for notifications for messages
     * mSystemSwitch    Switch for notifications for system
     * mPostSwitch      Switch for notification of posts*/

    Switch mMsgSwitch, mSystemSwitch, mPostSwitch;

    //INITIALISE LISTENERS
    /**
     * Retrieves the current notifications of the current user.
     *
     *  Database Path:      root/(currentUser.getUid())/_notifications
     *  Usage:              ValueEventListener
     */
    ValueEventListener getNotificationSettings = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Notifications Values", "getNotificationSettings", "null");
                reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_msg").setValue("on");
                reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_system").setValue("on");
                reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_post").setValue("on");
                return;
            }

            String messageNotification = (String) dataSnapshot.child("_msg").getValue();

            String systemNotification = (String) dataSnapshot.child("_system").getValue();

            String  postNotification = (String) dataSnapshot.child("_post").getValue();

             if(messageNotification == null) {
                logHandler.printDatabaseResultLog(".child(\"_msg\").getValue()", "messageNotification", "getNotificationSettings", "null");
                reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_msg").setValue("on");

                return;
            }
             else if(systemNotification == null) {
                 logHandler.printDatabaseResultLog(".child(\"_system\").getValue()", "systemNotification", "getNotificationSettings", "null");
                 reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_system").setValue("on");
                 return;
             }
             else if(postNotification == null) {
                 logHandler.printDatabaseResultLog(".child(\"_post\").getValue()", "postNotification", "getNotificationSettings", "null");
                 reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_post").setValue("on");

                 return;
             }


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

    //DEFAULT SUPER METHODS
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
        //SETUP VIEW OBJECTS

        //Populate Message Notifications Switch with Listener
        mMsgSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mMsgSwitch.isChecked()){
                    Notifications.subscribeMsgNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_msg").setValue("on");
                    logHandler.printLogWithMessage("Switch for messages notifications is turned on");
                }
                else{
                    Notifications.unsubscribeMsgNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_msg").setValue("off");
                    logHandler.printLogWithMessage("Switch for messages notifications is turned off");
                }
            }
        });

        //Populate System Notifications Switch with Listener
        mSystemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mSystemSwitch.isChecked()){
                    Notifications.subscribeSystemNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_system").setValue("on");
                    logHandler.printLogWithMessage("Switch for system notifications is turned on");
                }
                else{
                    Notifications.unsubscribeSystemNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_system").setValue("off");
                    logHandler.printLogWithMessage("Switch for system notifications is turned off");
                }
            }
        });

        //Populate Post Notifications Switch with Listener
        mPostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mPostSwitch.isChecked()){
                    Notifications.subscribePostsNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_post").setValue("on");
                    logHandler.printLogWithMessage("Switch for posts notifications is turned on");

                }
                else{
                    Notifications.unsubscribePostsNotification(logHandler,databaseRef);
                    reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_post").setValue("off");
                    logHandler.printLogWithMessage("Switch for posts notifications is turned off");
                }
            }
        });
    }

    @Override
    protected void AttachOnStartListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .child("_notifications")
                .addValueEventListener(getNotificationSettings);
    }

    @Override
    protected void DestroyOnStartListeners() {
        if(getNotificationSettings != null) {
            databaseRef.removeEventListener(getNotificationSettings);
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return R.id.notificationsMenuItem;
    }
}
