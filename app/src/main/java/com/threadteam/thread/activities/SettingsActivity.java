package com.threadteam.thread.activities;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.threadteam.thread.R;
import com.threadteam.thread.abstracts.MainBaseActivity;

/**
 * This activity class handles the notification settings of user.
 *
 * @author Mohamed Thabith
 * NOT IN USE
 */

public class SettingsActivity extends MainBaseActivity {
    /** Displays notifications navigation card view*/
    CardView mNotificationsCardView;

    //DEFAULT SUPER METHODS
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
        //SETUP VIEW OBJECTS

        //Populate Notifications Card View with Listener
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
    protected void AttachOnStartListeners() {

    }

    @Override
    protected void DestroyOnStartListeners() {

    }

    @Override
    protected int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }
}
