package com.threadteam.thread.abstracts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.threadteam.thread.LogHandler;

import java.util.ArrayList;
import java.util.HashMap;

import com.threadteam.thread.R;
import com.threadteam.thread.activities.LoginActivity;
import com.threadteam.thread.adapters.ViewServerAdapter;
import com.threadteam.thread.interfaces.APIService;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.notifications.Client;
import com.threadteam.thread.notifications.NotificationModel;
import com.threadteam.thread.notifications.Sender;
import com.threadteam.thread.notifications.ThreadResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Represents the standard basic activity layer for threadapp.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 */

public abstract class BaseActivity extends AppCompatActivity {

    // LOGGING
    protected LogHandler logHandler;

    // FIREBASE

    /** The current FirebaseUser object for the session */
    protected FirebaseUser currentUser;

    /** The current FirebaseAuth object for the session */
    protected FirebaseAuth firebaseAuth;

    /** The current DatabaseReference object for the session */
    protected DatabaseReference databaseRef;

    // DATA STORE

    /** The current title for the activity. */
    private String title;

    /** The context for the current activity. Used to get context from child classes */
    protected AppCompatActivity currentActivity;

    // NOTIFICATIONS

    /** The current APIService object used for notifications */
    APIService apiService;

    // VIEW OBJECTS

    /** The menu of the bottom toolbar. */
    protected ActionMenuView BottomToolbarAMV;

    /** The top navigation bar. Handles the menu and title of the current activity. */
    protected Toolbar TopNavToolbar;

    /** The main action button of the bottom toolbar. Should be used for prominent primary actions in an activity. */
    private ImageButton MainActionButton;

    /** The current menu item related to the activity. Should correspond to the menu being loaded into BottomToolbarAMV */
    private ActionMenuItemView CurrentMenuItem;

    /**
     * The default onCreate event for a basic activity. Uses abstracts to implement functionality from its subclasses.
     * @param savedInstanceState Default parameter for the onCreate event.
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayoutIDForContentView());

        currentActivity = setCurrentActivity();
        title = setTitleForActivity();
        logHandler = new LogHandler(title + " Activity");

        logHandler.printDefaultLog(LogHandler.STATE_ON_CREATE);

        BindTopNavToolbar();
        BindBottomToolbarAMV();
        logHandler.printDefaultLog(LogHandler.TOOLBAR_BOUND);

        SetupToolbars();
        MainActionButton = setMainActionButton();
        DoAdditionalSetupForToolbars();
        logHandler.printDefaultLog(LogHandler.TOOLBAR_SETUP);

        BindViewObjects();
        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);

        SetupViewObjects();
        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_SETUP);

        // NOTIFICATIONS
        apiService = Client.getClient().create(APIService.class);


        HandleIntentExtras();
        HandleAdditionalIntentExtras();
        InitialiseFirebase();
        DoAdditionalSetupForFirebase();

        logHandler.printDefaultLog(LogHandler.FIREBASE_INITIALISED);

        logHandler.printDefaultLog(LogHandler.FIREBASE_LISTENERS_INITIALISED);
    }

    /**
     * The default onStart event for a basic activity.
     * Listeners are attached here, if any.
     */

    @Override
    protected void onStart() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_START);
        AttachListeners();

        super.onStart();
    }

    /**
     * The default onResume event for a basic activity.
     * The bottom toolbar menu is redrawn here.
     */

    @Override
    protected void onResume() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_RESUME);
        invalidateOptionsMenu();

        super.onResume();
    }

    /**
     * The default onRestart event for a basic activity.
     * The bottom toolbar menu is redrawn here.
     */

    @Override
    protected void onRestart() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_RESTART);
        invalidateOptionsMenu();

        super.onRestart();
    }

    /**
     * The default onStop event for a basic activity.
     * Listeners are detached and destroyed here, if any.
     */

    @Override
    protected void onStop() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_STOP);
        DestroyListeners();

        super.onStop();
    }

    /**
     * The default onPause event for a basic activity.
     * The current menu item is toggled back to normal here.
     * @see BaseActivity#toggleCurrentMenuItem(Boolean)
     */

    @Override
    protected void onPause() {
        toggleCurrentMenuItem(true);
        super.onPause();
    }

    /**
     * The default onPrepareOptionsMenu event for a basic activity.
     * The current menu item's opacity is reduced as feedback that the user is on that current activity, if possible.
     * @see BaseActivity#toggleCurrentMenuItem(Boolean)
     */

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(BottomToolbarAMV != null) {
            BindCurrentMenuItem();
            if(CurrentMenuItem != null) {
                toggleCurrentMenuItem(false);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * The default onCreateOptionsMenu event for a basic activity.
     * The menu for the top and bottom toolbars are inflated, if possible.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(BottomToolbarAMV != null && BottomToolbarAMV.getMenu().size() == 0) {
            getMenuInflater().inflate(setBottomToolbarMenuID(), BottomToolbarAMV.getMenu());
        }

        if(TopNavToolbar != null) {
            AddItemsToTopNavToolbar();
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Abstract function for subclasses to implement.
     * Should return the layout id to be used in setContentView().
     */

    protected abstract int setLayoutIDForContentView();

    /**
     * Abstract function for subclasses to implement.
     * Should return the current class context.
     */

    protected abstract AppCompatActivity setCurrentActivity();

    /**
     * Abstract function for subclasses to implement.
     * Should return the user-friendly title of the current activity.
     */

    protected abstract String setTitleForActivity();

    /**
     * Abstract function for subclasses to implement.
     * Should return a configured ImageButton to be used in the current activity.
     */

    protected abstract ImageButton setMainActionButton();

    /**
     * Abstract function for subclasses to implement.
     * Should return the view id of the top navigation bar's include view object.
     * Else return null if there is no top navigation bar include view object for the current layout.
     */

    protected abstract Integer setTopNavToolbarIncludeId();

    /**
     * Takes in the id specified by setTopNavToolbarIncludeId().
     * Attempts to bind the top navigation bar object to its associated view
     */

    private void BindTopNavToolbar() {
        Integer includeId = setTopNavToolbarIncludeId();
        if(includeId != null) {
            View topNavView = findViewById(includeId);
            TopNavToolbar = (Toolbar) topNavView.findViewById(R.id.topNavToolbar);
        }
    }

    /**
     * Abstract function for subclasses to implement.
     * Should return the view id of the bottom toolbar's include view object.
     * Else return null if there is no top navigation bar include view object for the current layout.
     */

    protected abstract Integer setBottomToolbarAMVIncludeId();

    /**
     * Takes in the id specified by setBottomToolbarAMVIncludeId().
     * Attempts to bind the top navigation bar object to its associated view
     */

    private void BindBottomToolbarAMV() {
        Integer includeId = setBottomToolbarAMVIncludeId();
        if(includeId != null) {
            View bottomToolbarView = findViewById(includeId);
            BottomToolbarAMV = (ActionMenuView) bottomToolbarView.findViewById(R.id.bottomToolbarAMV);
        }
    }

    /**
     * Does a generic setup for both top and bottom toolbars.
     * Specifically, adds the title for the top navigation bar and links the bottom toolbar menu to the option handler.
     */

    private void SetupToolbars() {
        if(TopNavToolbar != null) {
            currentActivity.setSupportActionBar(TopNavToolbar);
            TopNavToolbar.setTitle(title);
        }

        if(BottomToolbarAMV != null) {
            BottomToolbarAMV.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
        }
    }

    /**
     * Abstract function for subclasses to implement.
     * Should run any further toolbar configuration code for the activity required.
     */

    protected abstract void DoAdditionalSetupForToolbars();

    /**
     * Abstract function for subclasses to implement.
     * Binding of subclass objects to their associated view should be implemented here.
     */

    protected abstract void BindViewObjects();

    /**
     * Abstract function for subclasses to implement.
     * Setup of subclass objects that have been bound should be implemented here.
     */

    protected abstract void SetupViewObjects();

    /**
     * Abstract function for subclasses to implement.
     * Should handle any incoming intent extras to the current activity.
     * Optimally, bind these values to an object to save them for later use.
     */

    protected abstract void HandleIntentExtras();

    /**
     * Abstract function for subclasses to implement.
     * Second additional layer for HandleIntentExtras.
     */

    protected abstract void HandleAdditionalIntentExtras();

    /**
     * Does a generic setup for basic Firebase values.
     * Specifically, retrieves the current user and database reference for the activity.
     * Also sends the user back to the login page if the current user is not found.
     */

    private void InitialiseFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null) {
            logHandler.printDefaultLog(LogHandler.FIREBASE_USER_NOT_FOUND);
            Intent returnToLogin = new Intent(currentActivity.getBaseContext(), LoginActivity.class);
            startActivity(returnToLogin);
            finish();
            return;
        }
        logHandler.printDefaultLog(LogHandler.FIREBASE_USER_FOUND);

        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Abstract function for subclasses to implement.
     * Should run any further Firebase configuration code for the activity required.
     */

    protected abstract void DoAdditionalSetupForFirebase();

    /**
     * Abstract function for subclasses to implement.
     * Should attach any Firebase event listeners required for the activity on startup here.
     */

    protected abstract void AttachListeners();

    /**
     * Abstract function for subclasses to implement.
     * Should destroy every Firebase event listener created in the activity.
     */

    protected abstract void DestroyListeners();

    /** Indicates that there is no corresponding menu item for the current activity. */
    protected static int NO_MENU_ITEM_FOR_ACTIVITY = -9999;

    /**
     * Abstract function for subclasses to implement.
     * Should return the view id of the corresponding menu item for the current activity.
     */

    protected abstract int setCurrentMenuItemID();

    /**
     * Binds the current menu item to its corresponding view object using setCurrentMenuItemID()'s id.
     */

    private void BindCurrentMenuItem() {
        if(setCurrentMenuItemID() != NO_MENU_ITEM_FOR_ACTIVITY) {
            CurrentMenuItem = (ActionMenuItemView) currentActivity.findViewById(setCurrentMenuItemID());
        }
    }

    /**
     * Abstract function for subclasses to implement.
     * Should return the menu id of the menu resource to be inflated in the bottom toolbar.
     */

    protected abstract int setBottomToolbarMenuID();

    /**
     * Abstract function for subclasses to implement.
     * Should return a HashMap of menu item id : menu item titles of menu items to be added to the top nav toolbar.
     */

    protected abstract HashMap<Integer, String> setItemsForTopNavToolbar(HashMap<Integer, String> itemHashMap);

    /**
     * Uses the HashMap returned by setItemsForTopNavToolbar() to add new menu items to the top nav toolbar.
     */

    private void AddItemsToTopNavToolbar() {
        HashMap<Integer, String> menuItemsMap = setItemsForTopNavToolbar(new HashMap<Integer, String>());

        if(menuItemsMap != null) {
            for(Integer menuItemID : menuItemsMap.keySet()) {
                TopNavToolbar.getMenu().add(Menu.NONE, menuItemID, Menu.NONE, menuItemsMap.get(menuItemID));
            }
        }
    }

    // PROTECTED CONVENIENCE METHODS

    /**
     * Toggles the current menu item's opacity.
     * If enabled is true, opacity is set to high. Otherwise, it is set to low opacity (disabled)
     */

    @SuppressLint("RestrictedApi")
    protected void toggleCurrentMenuItem(Boolean enabled) {
        if(CurrentMenuItem == null) {
            logHandler.printLogWithMessage("Can't find current menu item for " + title + "! Cancelling icon update!");
            return;
        }

        Drawable drawable = CurrentMenuItem.getItemData().getIcon();

        if(drawable == null) {
            logHandler.printLogWithMessage("Drawable for current menu item not found! Cancelling icon update!");
        } else {
            if(enabled) {
                drawable.setColorFilter(null);
            } else {
                drawable.setColorFilter(Color.argb(40, 255, 255, 255), PorterDuff.Mode.MULTIPLY);
            }
            CurrentMenuItem.setIcon(drawable);

            logHandler.printLogWithMessage("Successfully toggled current menu item for " + title + " to " + enabled.toString());
        }
    }

    /**
     * This function attempts to hide the software keyboard if it is being shown.
     */

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) currentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view  =currentActivity.getCurrentFocus();
        if (view == null) {
            view = new View(currentActivity);
        }
        if (imm != null) {
            logHandler.printLogWithMessage("Hiding keyboard!");
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Sends a notification to all members in a server
     * Author: Thabith
     *
     * @param serverId The current server's id
     * @param userId The current user's id
     * @param message The message payload for the notification
     */

    protected void sendNotification(final String serverId, final String userId, final String message){
        logHandler.printLogWithMessage("sendNotification invoked " + serverId  + ", " + userId + ", " + message);

        ValueEventListener getServerName = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String serverName = (String) dataSnapshot.getValue();

                ValueEventListener getUsername = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = (String) dataSnapshot.getValue();

                        final String to = "/topics/" + serverId;

                        final String body = username + message;

                        logHandler.printDatabaseResultLog(".getValue()", "Current Server Name", "getServerName", serverName);

                        logHandler.printLogWithMessage("Values sent to Api Service: " + to + ", " + serverName + ", " + body);

                        Sender sender = new Sender(to, new NotificationModel(serverName, body));
                        Call<ThreadResponse> threadResponseCall = apiService.sendNotification(sender);

                        threadResponseCall.enqueue(new Callback<ThreadResponse>() {
                            @Override
                            public void onResponse(Call<ThreadResponse> call, Response<ThreadResponse> response) {
                                logHandler.printLogWithMessage("Successfully sent notification by using retrofit.");
                            }

                            @Override
                            public void onFailure(Call<ThreadResponse> call, Throwable t) {
                                logHandler.printLogWithMessage("Unsuccessful at sending notification by using retrofit.");

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        logHandler.printDatabaseErrorLog(databaseError);
                    }
                };

                databaseRef.child("users")
                        .child(currentUser.getUid())
                        .child("_username")
                        .addListenerForSingleValueEvent(getUsername);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        databaseRef.child("servers")
                .child(serverId)
                .child("_name")
                .addListenerForSingleValueEvent(getServerName);

    }

    /**
     * Sends a system notification to all members in a server
     * Author: Thabith
     *
     * @param serverId The current server's id
     * @param userId The current user's id
     * @param message The message payload for the notification
     */

    protected void sendSystemNotification(final String serverId, final String userId, final String message){
        logHandler.printLogWithMessage("sendNotification invoked " + serverId  + ", " + userId + ", " + message);

        ValueEventListener getServerName = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String serverName = (String) dataSnapshot.getValue();

                ValueEventListener getUsername = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = (String) dataSnapshot.getValue();

                        final String to = "/topics/system" + serverId;

                        final String body = username + message;

                        logHandler.printDatabaseResultLog(".getValue()", "Current Server Name", "getServerName", serverName);

                        logHandler.printLogWithMessage("Values sent to Api Service: " + to + ", " + serverName + ", " + body);

                        Sender sender = new Sender(to, new NotificationModel(serverName, body));
                        Call<ThreadResponse> threadResponseCall = apiService.sendNotification(sender);

                        threadResponseCall.enqueue(new Callback<ThreadResponse>() {
                            @Override
                            public void onResponse(Call<ThreadResponse> call, Response<ThreadResponse> response) {
                                logHandler.printLogWithMessage("Successfully sent notification by using retrofit.");
                            }

                            @Override
                            public void onFailure(Call<ThreadResponse> call, Throwable t) {
                                logHandler.printLogWithMessage("Unsuccessful at sending notification by using retrofit.");

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        logHandler.printDatabaseErrorLog(databaseError);
                    }
                };

                databaseRef.child("users")
                        .child(currentUser.getUid())
                        .child("_username")
                        .addListenerForSingleValueEvent(getUsername);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        databaseRef.child("servers")
                .child(serverId)
                .child("_name")
                .addListenerForSingleValueEvent(getServerName);



    }

    /**
     * Sends a posts notification to all members in a server
     * Author: Thabith
     *
     * @param serverId The current server's id
     * @param userId The current user's id
     * @param message The message payload for the notification
     */

    protected void sendPostNotification(final String serverId, final String userId, final String message){
        logHandler.printLogWithMessage("sendNotification invoked " + serverId  + ", " + userId + ", " + message);

        ValueEventListener getServerName = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String serverName = (String) dataSnapshot.getValue();

                ValueEventListener getUsername = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = (String) dataSnapshot.getValue();

                        final String to = "/topics/posts" + serverId;

                        final String body = username + message;

                        logHandler.printDatabaseResultLog(".getValue()", "Current Server Name", "getServerName", serverName);

                        logHandler.printLogWithMessage("Values sent to Api Service: " + to + ", " + serverName + ", " + body);

                        Sender sender = new Sender(to, new NotificationModel(serverName, body));
                        Call<ThreadResponse> threadResponseCall = apiService.sendNotification(sender);

                        threadResponseCall.enqueue(new Callback<ThreadResponse>() {
                            @Override
                            public void onResponse(Call<ThreadResponse> call, Response<ThreadResponse> response) {
                                logHandler.printLogWithMessage("Successfully sent notification by using retrofit.");
                            }

                            @Override
                            public void onFailure(Call<ThreadResponse> call, Throwable t) {
                                logHandler.printLogWithMessage("Unsuccessful at sending notification by using retrofit.");

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        logHandler.printDatabaseErrorLog(databaseError);
                    }
                };

                databaseRef.child("users")
                        .child(currentUser.getUid())
                        .child("_username")
                        .addListenerForSingleValueEvent(getUsername);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        databaseRef.child("servers")
                .child(serverId)
                .child("_name")
                .addListenerForSingleValueEvent(getServerName);
    }

}
