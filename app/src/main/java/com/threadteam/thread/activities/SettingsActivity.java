package com.threadteam.thread.activities;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.threadteam.thread.R;
import com.threadteam.thread.abstracts.MainBaseActivity;

// VIEW PROFILE ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// MOHAMED THABITH, S10196396BB
//
// DESCRIPTION
// Handles settings page
//
// NAVIGATION
// PARENT: View Servers
// CHILDREN: Set Notifications
// OTHER: NONE

public class SettingsActivity extends MainBaseActivity {

    CardView mNotificationsCardView;


    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_settings;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return SettingsActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "Settings";
    }

    @Override
    protected ImageButton setMainActionButton() {
        return null;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.SettingsNavbarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return null;
    }

    @Override
    protected void DoAdditionalSetupForToolbars() {
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void BindViewObjects() {
        mNotificationsCardView = (CardView) findViewById(R.id.notificationRedirect);
    }

    @Override
    protected void SetupViewObjects() {
        mNotificationsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logHandler.printLogWithMessage("User tapped on Notifications!");
                Intent goToNotiSettings = new Intent(currentActivity, SetNotificationsActivity.class);
                startActivity(goToNotiSettings);
                logHandler.printActivityIntentLog("Set Notifications Activity");
            }
        });
    }

    @Override
    protected void AttachListeners() {

    }

    @Override
    protected void DestroyListeners() {

    }

    @Override
    protected int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }
}
