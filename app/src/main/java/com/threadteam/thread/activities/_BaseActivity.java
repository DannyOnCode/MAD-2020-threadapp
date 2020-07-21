package com.threadteam.thread.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.LogHandler;

import java.util.HashMap;

import com.threadteam.thread.R;
import com.threadteam.thread.interfaces.APIService;
import com.threadteam.thread.notifications.Client;
import com.threadteam.thread.notifications.Data;
import com.threadteam.thread.notifications.Sender;
import com.threadteam.thread.notifications.ThreadResponse;
import com.threadteam.thread.notifications.Token;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class _BaseActivity extends AppCompatActivity {

    // LOGGING
    protected LogHandler logHandler;

    // FIREBASE
    //
    // currentUser:             CURRENT USER FOR THE CURRENT SESSION
    // firebaseAuth:            FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION
    // databaseRef:             FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION

    protected FirebaseUser currentUser;
    protected FirebaseAuth firebaseAuth;
    protected DatabaseReference databaseRef;

    // DATA STORE
    //
    // title                    TITLE OF THE CURRENT ACTIVITY
    // currentActivity          CURRENT ACTIVITY CONTEXT

    private String title;
    protected AppCompatActivity currentActivity;

    // NOTIFICATIONS
    //
    // apiService               THE API SERVICE OBJECT FOR NOTIFICATIONS
    APIService apiService;

    // VIEW OBJECTS
    //
    // BottomToolbarAMV:        HANDLES THE MENU FOR THE BOTTOM TOOLBAR.
    // TopNavToolbar:           TOOLBAR OBJECT THAT HANDLES UPWARDS NAVIGATION AND THE TITLE
    // MainActionButton:        BOTTOM TOOLBAR MAIN ACTION BUTTON. USED TO HANDLE MAIN ACTIONS ON THIS
    //                          ACTIVITY. BOUND TO NAVIGATING TO ADD SERVER ACTIVITY IN THIS INSTANCE.
    // CurrentMenuItem:         THE ACTION MENU ITEM FOR THE CURRENT ACTIVITY IF THE ACTIVITY CORRESPONDS
    //                          TO A MENU ITEM IN BottomToolbarAMV

    protected ActionMenuView BottomToolbarAMV;
    protected Toolbar TopNavToolbar;
    private ImageButton MainActionButton;
    private ActionMenuItemView CurrentMenuItem;

    // CONSTANTS
    protected static int NO_MENU_ITEM_FOR_ACTIVITY = -9999;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayoutIDForContentView());

        currentActivity = setCurrentActivity();
        title = setTitleForActivity();
        logHandler = new LogHandler(title + " Activity");

        logHandler.printDefaultLog(LogHandler.STATE_ON_CREATE);

        TopNavToolbar = setTopNavToolbar();
        BottomToolbarAMV = setBottomToolbarAMV();
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
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        HandleIntentExtras();
        HandleAdditionalIntentExtras();
        InitialiseFirebase();
        DoAdditionalSetupForFirebase();

        logHandler.printDefaultLog(LogHandler.FIREBASE_INITIALISED);

        logHandler.printDefaultLog(LogHandler.FIREBASE_LISTENERS_INITIALISED);
    }

    @Override
    protected void onStart() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_START);
        AttachListeners();

        super.onStart();
    }

    @Override
    protected void onResume() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_RESUME);
        invalidateOptionsMenu();

        super.onResume();
    }

    @Override
    protected void onRestart() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_RESTART);
        invalidateOptionsMenu();

        super.onRestart();
    }

    @Override
    protected void onStop() {
        logHandler.printDefaultLog(LogHandler.STATE_ON_STOP);
        DestroyListeners();
        toggleCurrentMenuItem(true);

        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(BottomToolbarAMV != null) {
            setCurrentMenuItem();
            if(CurrentMenuItem != null) {
                toggleCurrentMenuItem(false);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

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

    abstract int setLayoutIDForContentView();

    abstract AppCompatActivity setCurrentActivity();
    abstract String setTitleForActivity();
    abstract ImageButton setMainActionButton();

    abstract Toolbar setTopNavToolbar();
    abstract ActionMenuView setBottomToolbarAMV();

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

    abstract void DoAdditionalSetupForToolbars();

    abstract void BindViewObjects();
    abstract void SetupViewObjects();

    abstract void HandleIntentExtras();
    abstract void HandleAdditionalIntentExtras();

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

    abstract void DoAdditionalSetupForFirebase();

    abstract void AttachListeners();
    abstract void DestroyListeners();

    abstract int setCurrentMenuItemID();

    private void setCurrentMenuItem() {
        if(setCurrentMenuItemID() != NO_MENU_ITEM_FOR_ACTIVITY) {
            CurrentMenuItem = (ActionMenuItemView) currentActivity.findViewById(setCurrentMenuItemID());
        }
    }

    abstract int setBottomToolbarMenuID();
    abstract HashMap<Integer, String> setItemsForTopNavToolbar(HashMap<Integer, String> itemHashMap);

    private void AddItemsToTopNavToolbar() {
        HashMap<Integer, String> menuItemsMap = setItemsForTopNavToolbar(new HashMap<Integer, String>());

        if(menuItemsMap != null) {
            for(Integer menuItemID : menuItemsMap.keySet()) {
                TopNavToolbar.getMenu().add(Menu.NONE, menuItemID, Menu.NONE, menuItemsMap.get(menuItemID));
            }
        }
    }

    // PROTECTED CONVENIENCE METHODS

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

    protected void sendNotification(String receiver, final String username, final String message, final String _serverId){
        DatabaseReference tokens = databaseRef.child("tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(currentUser.getUid(), R.mipmap.ic_launcher, username+": "+message, "New Message", _serverId);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<ThreadResponse>() {
                                @Override
                                public void onResponse(Call<ThreadResponse> call, Response<ThreadResponse> response) {
                                    if (response.code() ==200){
                                        if (response.body().success !=1){
                                            Toast.makeText(currentActivity,"Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ThreadResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
