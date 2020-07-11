package com.threadteam.thread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.threadteam.thread.R;

public abstract class MainBaseActivity extends BaseActivity {

    // DATA STORE
    //
    // LOG_OUT_MENU_ITEM_ID:    CONSTANT DECLARING ID FOR THE LOG OUT MENU ITEM.

    protected final int LOG_OUT_MENU_ITEM_ID = -1;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.viewServersMenuItem:
                logHandler.printLogWithMessage("User tapped on View Servers Menu Item!");

                if(currentActivity.getClass() != ViewServersActivity.class) {
                    Intent goToViewServers = new Intent(currentActivity, ViewServersActivity.class);
                    currentActivity.startActivity(goToViewServers);
                    logHandler.printActivityIntentLog("View Servers");

                    toggleCurrentMenuItem(true);
                }
                return true;

            case R.id.viewProfileMenuItem:
                logHandler.printLogWithMessage("User tapped on View Profile Menu Item!");

                if(currentActivity.getClass() != ViewProfileActivity.class) {
                    Intent goToViewProfile = new Intent(currentActivity, ViewProfileActivity.class);
                    currentActivity.startActivity(goToViewProfile);
                    logHandler.printActivityIntentLog("View Profile");

                    toggleCurrentMenuItem(true);
                }
                return true;

            case LOG_OUT_MENU_ITEM_ID:
                logHandler.printLogWithMessage("User tapped on Log Out Menu Item!");
                firebaseAuth.signOut();

                Intent goToLogin = new Intent(currentActivity, LoginActivity.class);
                startActivity(goToLogin);
                logHandler.printActivityIntentLog("Login Activity");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // DEFAULT SUPER METHODS

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
