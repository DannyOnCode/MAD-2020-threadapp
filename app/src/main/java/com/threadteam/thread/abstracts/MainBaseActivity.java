package com.threadteam.thread.abstracts;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.threadteam.thread.R;
import com.threadteam.thread.activities.LoginActivity;
import com.threadteam.thread.activities.ViewProfileActivity;
import com.threadteam.thread.activities.ViewServersActivity;

import java.util.HashMap;

/**
 * Represents the standard activity layer for threadapp's main activities.
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 */

public abstract class MainBaseActivity extends BaseActivity {

    // DATA STORE
    //
    // LOG_OUT_MENU_ITEM_ID:    CONSTANT DECLARING ID FOR THE LOG OUT MENU ITEM.

    /** Constant declaring the menu item id for the Log Out menu item */
    private final int LOG_OUT_MENU_ITEM_ID = -1;

    /**
     * Handles the onOptionsItemSelected event. Should be used by all subclasses to provide consistent navigation and features.
     * @param item The item that was selected by the user.
     * @return A boolean indicating whether the event has been completely handled.
     */

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

    // ABSTRACT OVERRIDE METHODS

    /**
     * {@inheritDoc}
     * This implementation adds the Log Out menu item to the top navigation bar.
     * @param itemHashMap
     * @return
     */

    @Override
    protected HashMap<Integer, String> setItemsForTopNavToolbar(HashMap<Integer, String> itemHashMap) {
        itemHashMap.put(LOG_OUT_MENU_ITEM_ID, "Log Out");
        return itemHashMap;
    }

    @Override
    protected void HandleIntentExtras() { }

    @Override
    protected void HandleAdditionalIntentExtras(){ }

    @Override
    protected void DoAdditionalSetupForToolbars() { }

    @Override
    protected void DoAdditionalSetupForFirebase() { }

    @Override
    protected int setBottomToolbarMenuID() {
        return R.menu.bottom_app_bar_menu;
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
