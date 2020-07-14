package com.threadteam.thread.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public abstract class _ServerBaseActivity extends _BaseActivity {

    // DATA STORE
    //
    // SHARE_SERVER_MENU_ITEM:  CONSTANT DECLARING ID FOR THE SHARE SERVER MENU ITEM.
    // LEAVE_SERVER_MENU_ITEM:  CONSTANT DECLARING ID FOR THE LEAVE SERVER MENU ITEM.
    // shareCode:               CONTAINS THE CURRENT SHARING CODE OF THE SERVER (IF IT EXISTS)
    //                          ALSO WORKS AS A FLAG FOR THE resetShareCode FUNCTION.
    // serverId:                CONTAINS CURRENT SERVER ID
    // isOwner:                 CONTAINS DATA ON WHETHER THE CURRENT USER IS THE OWNER OF THE SERVER

    private final int SHARE_SERVER_MENU_ITEM = -1;
    private final int LEAVE_SERVER_MENU_ITEM = -2;

    private final String IS_OWNER_KEY = "IS_OWNER";
    private final String SERVER_ID_KEY = "SERVER_ID";

    private String shareCode = null;

    protected String serverId;
    private Boolean isOwner;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.postsMenuItem:
                logHandler.printLogWithMessage("User tapped on Posts Menu Item!");

                if(currentActivity.getClass() != PostsActivity.class) {
                    Intent goToPosts = new Intent(currentActivity, PostsActivity.class);
                    PutExtrasForServerIntent(goToPosts);
                    currentActivity.startActivity(goToPosts);
                    logHandler.printActivityIntentLog("Posts");

                    toggleCurrentMenuItem(true);
                }
                return true;

            case R.id.chatMenuItem:
                logHandler.printLogWithMessage("User tapped on Chat Menu Item!");

                Intent goToChat = new Intent(currentActivity, ChatActivity.class);
                PutExtrasForServerIntent(goToChat);
                currentActivity.startActivity(goToChat);
                logHandler.printActivityIntentLog("Chats");

                toggleCurrentMenuItem(true);
                return true;

            case R.id.membersMenuItem:
                logHandler.printLogWithMessage("User tapped on Members Menu Item!");

                if(currentActivity.getClass() != ViewMembersActivity.class) {
                    Intent goToViewMembers = new Intent(currentActivity, ViewMembersActivity.class);
                    PutExtrasForServerIntent(goToViewMembers);
                    currentActivity.startActivity(goToViewMembers);
                    logHandler.printActivityIntentLog("View Members");

                    toggleCurrentMenuItem(true);
                }
                return true;

            case R.id.settingsMenuItem:
                logHandler.printLogWithMessage("User tapped on Settings Menu Item!");

                if(currentActivity.getClass() != ServerSettingsActivity.class) {
                    Intent goToServerSettings = new Intent(currentActivity, ServerSettingsActivity.class);
                    PutExtrasForServerIntent(goToServerSettings);
                    currentActivity.startActivity(goToServerSettings);
                    logHandler.printActivityIntentLog("Server Settings");

                    toggleCurrentMenuItem(true);
                }
                return true;

            case android.R.id.home:
                logHandler.printLogWithMessage("User tapped on Back Button!");
                Intent returnToViewServers = new Intent(currentActivity, ViewServersActivity.class);
                currentActivity.startActivity(returnToViewServers);
                logHandler.printActivityIntentLog("View Servers");
                return true;

            case SHARE_SERVER_MENU_ITEM:
                showShareServerPopup(setBaseLayer());
                return true;

            case LEAVE_SERVER_MENU_ITEM:
                handleLeaveServerAlert();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ABSTRACT METHOD DECLARATIONS

    abstract ConstraintLayout setBaseLayer();

    // ABSTRACT OVERRIDE METHODS

    @Override
    HashMap<Integer, String> setItemsForTopNavToolbar(HashMap<Integer, String> itemHashMap) {
        itemHashMap.put(SHARE_SERVER_MENU_ITEM, "Share Server");
        itemHashMap.put(LEAVE_SERVER_MENU_ITEM, "Leave Server");
        return itemHashMap;
    }

    @Override
    void HandleIntentExtras() {
        final Intent dataReceiver = getIntent();

        String isOwnerString = dataReceiver.getStringExtra(IS_OWNER_KEY);
        serverId = dataReceiver.getStringExtra(SERVER_ID_KEY);

        if (isOwnerString == null) {
            logHandler.printGetExtrasResultLog(IS_OWNER_KEY, "null");
        } else {
            logHandler.printGetExtrasResultLog(IS_OWNER_KEY, isOwnerString);
            isOwner = isOwnerString.equals(((Boolean) true).toString());
        }

        if (serverId == null) {
            logHandler.printGetExtrasResultLog(SERVER_ID_KEY, "null");
        } else {
            logHandler.printGetExtrasResultLog(SERVER_ID_KEY, serverId);
        }
    }

    @Override
    void DoAdditionalSetupForToolbars() {
        if(currentActivity.getSupportActionBar() != null) {
            currentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    void DoAdditionalSetupForFirebase() { }

    @Override
    int setBottomToolbarMenuID() {
        return R.menu.server_menu;
    }

    // ACTIVITY SPECIFIC METHODS

    private void resetShareCode() {
        logHandler.printLogWithMessage("Resetting Share Code (if not null)!");

        if(shareCode != null) {
            databaseRef.child("shares").child(String.valueOf(shareCode)).setValue(null);
            shareCode = null;
        }
    }

    private void hideSettingsIfNotOwner() {
        ActionMenuItemView SettingsAMIV = (ActionMenuItemView) currentActivity.findViewById(R.id.settingsMenuItem);

        if(SettingsAMIV == null) {
            logHandler.printLogWithMessage("Can't find menu item for Settings! Cancelling visibility update!");
            return;
        }

        if(!isOwner){
            SettingsAMIV.setVisibility(View.INVISIBLE);
        }
    }

    private void returnToViewServers() {
        Intent returnToViewServers = new Intent(currentActivity, ViewServersActivity.class);
        startActivity(returnToViewServers);
        logHandler.printActivityIntentLog("View Servers Activity");

        resetShareCode();
    }

    private void showShareServerPopup(ConstraintLayout baseLayer) {
        // LOGGING FOR SHARE SERVER POPUP WINDOW
        LogHandler popupLogHandler = new LogHandler("ShareServerPopupWindow");

        popupLogHandler.printLogWithMessage("Configuring Popup!");

        View popupView = LayoutInflater.from(this.getBaseContext()).inflate(
                R.layout.activity_shareserver, baseLayer, false
        );

        final PopupWindow shareCodePopup = new PopupWindow(
                popupView,
                (int) (baseLayer.getWidth() * 0.8),
                (int) (baseLayer.getHeight() *0.8),
                true);
        shareCodePopup.setTouchable(true);
        shareCodePopup.showAtLocation(baseLayer, Gravity.CENTER, 0 ,0);
        shareCodePopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                resetShareCode();
            }
        });

        popupLogHandler.printLogWithMessage("Completed configuring Popup!");

        // BIND VIEW OBJECTS

        ConstraintLayout BaseShareCodeConstraintLayout = popupView
                .findViewById(R.id.baseShareCodeConstraintLayout);
        Toolbar PopupToolbar = popupView.findViewById(R.id.shareCodeToolbar);
        final Button RefreshCodeButton = popupView.findViewById(R.id.refreshCodeButton);
        final TextView ShareCodeTextView = popupView.findViewById(R.id.shareCodeTextView);
        final TextView ShareCodeDescTextView = popupView.findViewById(R.id.shareCodeDescTextView);
        final TextView CodeExpiryTextView = popupView.findViewById(R.id.codeExpiryTextView);

        popupLogHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);

        // SETUP VIEW OBJECTS

        PopupToolbar.setTitle("Share Server Code");
        RefreshCodeButton.setText("GET CODE");
        ShareCodeTextView.setText("------");

        final CountDownTimer codeTimer = new CountDownTimer(10*60*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long mins = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long secs = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - (mins * 60);
                CodeExpiryTextView.setText(String.format(Locale.ENGLISH, "expires in %02d:%02d", mins, secs));
            }

            @Override
            public void onFinish() {
                logHandler.printLogWithMessage("Timer has ended!");

                RefreshCodeButton.setText("GET CODE");
                ShareCodeTextView.setText("------");
                ShareCodeDescTextView.setText(R.string.share_code_noCode);
                CodeExpiryTextView.setText("expires in 00:00:00");

                resetShareCode();
            }
        };

        popupLogHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_SETUP);

        // INITIALISE LISTENERS

        // sharesListener:  CHECKS THAT THERE IS NO IDENTICAL SHARE CODE IN SHARES AND CHANGES THE SHARE CODE IF
        //                  NECESSARY, TILL THERE IS NO CONFLICT IN THE DATABASE. AFTER THIS, PAIRS THE SHARE CODE
        //                  WITH THE CURRENT SERVER ID AND REFLECTS THE UPDATE GRAPHICALLY BY UPDATING ShareCodeTextView,
        //                  RefreshCodeButton, ShareCodeDescTextView AND STARTS codeTimer.
        //                  CORRECT INVOCATION CODE: databaseRef.child("shares")
        //                                                      .child(shareCode)
        //                                                      .addListenerForSingleValueEvent(sharesListener)
        //                  SHOULD NOT BE USED INDEPENDENTLY.

        final ValueEventListener sharesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                while (dataSnapshot.getValue() != null) {
                    shareCode = Utils.GenerateAlphanumericID(6);
                }

                databaseRef.child("shares").child(shareCode).setValue(serverId);

                ShareCodeTextView.setText(shareCode);
                RefreshCodeButton.setText("REFRESH");
                ShareCodeDescTextView.setText(R.string.share_code_hasCode);
                codeTimer.start();

                logHandler.printLogWithMessage("Code generated successfully, timer started!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        popupLogHandler.printDefaultLog(LogHandler.FIREBASE_LISTENERS_INITIALISED);

        RefreshCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FOR REFRESH
                resetShareCode();
                codeTimer.cancel();
                logHandler.printLogWithMessage("Timer has been cancelled (if not null)!");

                // GET CODE
                shareCode = Utils.GenerateAlphanumericID(6);
                databaseRef.child("shares")
                        .child(shareCode)
                        .addListenerForSingleValueEvent(sharesListener);
            }
        });

        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCodePopup.dismiss();
                codeTimer.cancel();
            }
        });
    }

    private void handleLeaveServerAlert() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        final String currentUID = currentUser.getUid();
        builder.setTitle("Leave Server?");

        // deleteServer:    REMOVES ALL SUBSCRIPTIONS FOR A SINGLE SERVER ACROSS ALL ITS MEMBERS
        //                  INCLUDING THE OWNER, THEN DELETES ALL DATA PERTAINING TO IT AND SENDS
        //                  THE USER BACK TO THE ViewServers Activity
        //                  CORRECT INVOCATION CODE: databaseRef.child("members")
        //                                                      .child(serverId)
        //                                                      .addListenerForSingleValueEvent(deleteServer)
        //                  SHOULD NOT BE USED INDEPENDENTLY.

        final ValueEventListener deleteServer = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    if(data.getKey() == null) {
                        logHandler.printDatabaseResultLog(".getChildren().Key()", "Subscribed User ID", "deleteServer", "null");
                        continue;
                    }

                    String userID = data.getKey();
                    logHandler.printDatabaseResultLog(".getChildren().Key()", "Subscribed User ID", "deleteServer", userID);

                    databaseRef.child("users").child(userID).child("_subscribedServers").child(serverId).setValue(null);
                }

                // DELETE ALL SERVER DATA AFTER MEMBERS ARE GONE
                databaseRef.child("servers").child(serverId).setValue(null);
                databaseRef.child("messages").child(serverId).setValue(null);
                databaseRef.child("members").child(serverId).setValue(null);

                logHandler.printLogWithMessage("Server is completely deleted! Returning user back to View Server Activity!");
                returnToViewServers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        // presentDialog:   GETS OWNER ID AND COMPARES IT TO CURRENT USER'S ID, THEN ADAPTS THE ALERT DIALOG
        //                  ACCORDINGLY. FOLLOWING THIS, RUNS THE REST OF THE LEAVE SERVER LOGIC.
        //                  CORRECT INVOCATION CODE: databaseRef.child("servers")
        //                                                      .child(serverId)
        //                                                      .addListenerForSingleValueEvent(presentDialog)

        ValueEventListener presentDialog = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("_ownerID").getValue() == null) {
                    logHandler.printDatabaseResultLog(".child(\"_ownerID\").getValue()", "Server Owner ID", "getServerOwner", "null");
                    return;
                }

                String serverOwnerID = (String) dataSnapshot.child("_ownerID").getValue();

                if(serverOwnerID == null) {
                    logHandler.printLogWithMessage("Could not cast value of dataSnapshot.child(\"_ownerID\").getValue() to String! Aborting Leave Server function!");
                    return;
                }

                logHandler.printDatabaseResultLog(".child(\"_ownerID\").getValue()", "Server Owner ID", "getServerOwner", serverOwnerID);

                if(serverOwnerID.equals(currentUID)) {

                    // User is server owner
                    builder.setMessage("Are you sure you want to leave the server? As the owner, this will delete the server for everyone as well!");
                    builder.setNegativeButton("Yes, leave the server", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            logHandler.printLogWithMessage("Deleting server for all users!");
                            databaseRef.child("members")
                                    .child(serverId)
                                    .addListenerForSingleValueEvent(deleteServer);

                        }
                    });

                } else {

                    // User is just a subscriber
                    builder.setMessage("Are you sure you want to leave the server? You'll have to re-enter another Server Share Code if you want to rejoin the server!");
                    builder.setNegativeButton("Yes, leave the server", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            logHandler.printLogWithMessage("Removing server subscription for user!");
                            databaseRef.child("users").child(currentUID).child("_subscribedServers").child(serverId).setValue(null);
                            databaseRef.child("members").child(serverId).child(currentUID).setValue(null);

                            logHandler.printLogWithMessage("Returning user back to View Server Activity!");
                            returnToViewServers();
                        }
                    });

                }

                builder.setNeutralButton("Cancel", null);
                builder.create().show();
                logHandler.printLogWithMessage("Presenting Leave Server Dialog!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        databaseRef.child("servers")
                .child(serverId)
                .addListenerForSingleValueEvent(presentDialog);
    }

    // PROTECTED CONVENIENCE METHODS

    protected void PutExtrasForServerIntent(Intent intent) {
        intent.putExtra(SERVER_ID_KEY, serverId);
        intent.putExtra(IS_OWNER_KEY, isOwner.toString());
    }

    protected void SendSystemMessageInChat(@NonNull String message, @NonNull String _serverId) {
        HashMap<String, Object> chatMessageHashMap = new HashMap<>();
        chatMessageHashMap.put("_senderUID", "SYSTEM");
        chatMessageHashMap.put("_sender", "SYSTEM");
        chatMessageHashMap.put("_message", message);
        chatMessageHashMap.put("timestamp", System.currentTimeMillis());
        databaseRef.child("messages").child(_serverId).push().setValue(chatMessageHashMap);
    }

    protected void AddExpForServerMember(final String _userId, final String _serverId, final int _exp, int _secondsCooldown) {
        String PREF_FILE = "cooldownPref";
        String COOLDOWN_KEY = "cooldownFinish";

        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        long millisCooldownFinish = prefs.getLong(COOLDOWN_KEY, -1);
        long millisNow = System.currentTimeMillis();

        if(millisCooldownFinish == -1 || millisNow > millisCooldownFinish) {

            final ValueEventListener broadcastLevelUp = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String username = (String) dataSnapshot.getValue();

                    if(username == null) {
                        logHandler.printDatabaseResultLog(".getValue()", "Username", "broadcastLevelUp", "null");
                        return;
                    }
                    logHandler.printDatabaseResultLog(".getValue()", "Username", "broadcastLevelUp", username);

                    SendSystemMessageInChat(username + " has levelled up!", _serverId);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    logHandler.printDatabaseErrorLog(databaseError);
                }
            };

            final ValueEventListener updateExpListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null) {
                        logHandler.printDatabaseResultLog(".getValue()", "Server Exp", "updateExpListener", "null");
                        return;
                    }

                    Long currentExp = (Long) dataSnapshot.getValue();
                    logHandler.printDatabaseResultLog(".getValue()", "Server Exp", "updateExpListener", currentExp.toString());

                    databaseRef.child("users")
                            .child(_userId)
                            .child("_subscribedServers")
                            .child(_serverId)
                            .setValue(currentExp + _exp);

                    databaseRef.child("members")
                            .child(_serverId)
                            .child(_userId)
                            .setValue(currentExp + _exp);

                    // Feedback to user on level up
                    Integer oldLevel = Utils.ConvertExpToLevel(currentExp.intValue());
                    Integer newLevel = Utils.ConvertExpToLevel(currentExp.intValue() + _exp);
                    if(oldLevel < newLevel) {
                        //TODO: Maybe make level up dialog nicer? Will have to see if we've got time to implement this

                        // Announce level up in chat
                        databaseRef.child("users")
                                   .child(_userId)
                                   .child("_username")
                                   .addListenerForSingleValueEvent(broadcastLevelUp);

                        Toast.makeText(getApplicationContext(), "You leveled up! Current level: " + newLevel.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    logHandler.printDatabaseErrorLog(databaseError);
                }
            };

            databaseRef.child("users")
                    .child(_userId)
                    .child("_subscribedServers")
                    .child(_serverId)
                    .addListenerForSingleValueEvent(updateExpListener);

            // Write Cooldown time to sharedprefs
            SharedPreferences.Editor editor = prefs.edit();
            millisCooldownFinish = System.currentTimeMillis() + _secondsCooldown * 1000;
            editor.putLong(COOLDOWN_KEY, millisCooldownFinish);
            editor.apply();
        }
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
        hideSettingsIfNotOwner();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
