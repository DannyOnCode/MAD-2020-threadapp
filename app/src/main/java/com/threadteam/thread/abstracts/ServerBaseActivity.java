package com.threadteam.thread.abstracts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.threadteam.thread.activities.ChatActivity;
import com.threadteam.thread.activities.PostsActivity;
import com.threadteam.thread.activities.ServerSettingsActivity;
import com.threadteam.thread.activities.ViewMembersActivity;
import com.threadteam.thread.activities.ViewServersActivity;
import com.threadteam.thread.libraries.Progression;
import com.threadteam.thread.R;
import com.threadteam.thread.libraries.Utils;
import com.threadteam.thread.popups.ShareServerPopup;

import java.util.HashMap;

/**
 * Represents the standard activity layer for threadapp's server activities.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 */

public abstract class ServerBaseActivity extends BaseActivity {

    // DATA STORE

    // PRIVATE FIELDS

    /** The menu identifier for the Share Server Menu Item. */
    private final int SHARE_SERVER_MENU_ITEM = -1;

    /** The menu identifier for the Leave Server Menu Item. */
    private final int LEAVE_SERVER_MENU_ITEM = -2;

    /** The key for the IS_OWNER intent extra. */
    private final String IS_OWNER_KEY = "IS_OWNER";

    /** The key for the SERVER_ID intent extra. */
    private final String SERVER_ID_KEY = "SERVER_ID";

    /** The current Share Server Popup object for the server. */
    private ShareServerPopup popup;

    /** Flag indicating whether the current user is the owner of the current server */
    private Boolean isOwner;

    // PROTECTED FIELDS

    /** The current server's identifier. */
    protected String serverId;

    /**
     * Handles the onOptionsItemSelected event. Should be used by all subclasses to provide consistent navigation and features.
     * @param item The item that was selected by the user.
     * @return A boolean indicating whether the event has been completely handled.
     */

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

                    toggleCurrentMenuItem(false);
                }
                return true;

            case R.id.chatMenuItem:
                logHandler.printLogWithMessage("User tapped on Chat Menu Item!");

                Intent goToChat = new Intent(currentActivity, ChatActivity.class);
                PutExtrasForServerIntent(goToChat);
                currentActivity.startActivity(goToChat);
                logHandler.printActivityIntentLog("Chats");

                toggleCurrentMenuItem(false);
                return true;

            case R.id.membersMenuItem:
                logHandler.printLogWithMessage("User tapped on Members Menu Item!");

                if(currentActivity.getClass() != ViewMembersActivity.class) {
                    Intent goToViewMembers = new Intent(currentActivity, ViewMembersActivity.class);
                    PutExtrasForServerIntent(goToViewMembers);
                    currentActivity.startActivity(goToViewMembers);
                    logHandler.printActivityIntentLog("View Members");

                    toggleCurrentMenuItem(false);
                }
                return true;

            case R.id.settingsMenuItem:
                logHandler.printLogWithMessage("User tapped on Settings Menu Item!");

                if(currentActivity.getClass() != ServerSettingsActivity.class) {
                    Intent goToServerSettings = new Intent(currentActivity, ServerSettingsActivity.class);
                    PutExtrasForServerIntent(goToServerSettings);
                    currentActivity.startActivity(goToServerSettings);
                    logHandler.printActivityIntentLog("Server Settings");

                    toggleCurrentMenuItem(false);
                }
                return true;

            case android.R.id.home:
                logHandler.printLogWithMessage("User tapped on Back Button!");
                returnToViewServers();
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

    /**
     * Should return a ConstraintLayout object that is the root object of the activity's layout.
     * Used to present the Share Server popup.
     * @return A single ConstraintLayout that is the root object of the activity's layout.
     */

    protected abstract ConstraintLayout setBaseLayer();

    // ABSTRACT OVERRIDE METHODS

    /**
     * {@inheritDoc}
     * This implementation adds the Share Server and Leave Server menu items to the top navigation bar.
     */

    @Override
    protected HashMap<Integer, String> setItemsForTopNavToolbar(HashMap<Integer, String> itemHashMap) {
        itemHashMap.put(SHARE_SERVER_MENU_ITEM, "Share Server");
        itemHashMap.put(LEAVE_SERVER_MENU_ITEM, "Leave Server");
        return itemHashMap;
    }

    /**
     * {@inheritDoc}
     * This implementation gets and sets the IS_OWNER and SERVER_ID extras for later use.
     */

    @Override
    protected void HandleIntentExtras() {
        final Intent dataReceiver = getIntent();

        isOwner = dataReceiver.getBooleanExtra(IS_OWNER_KEY, false);
        serverId = dataReceiver.getStringExtra(SERVER_ID_KEY);

        if (serverId == null) {
            logHandler.printGetExtrasResultLog(SERVER_ID_KEY, "null");
        } else {
            logHandler.printGetExtrasResultLog(SERVER_ID_KEY, serverId);
        }
    }

    @Override
    protected void HandleAdditionalIntentExtras(){ }

    /**
     * {@inheritDoc}
     * This implementation enables the back button on the top navigation bar by default, if possible.
     */

    @Override
    protected void DoAdditionalSetupForToolbars() {
        if(currentActivity.getSupportActionBar() != null) {
            currentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void DoAdditionalSetupForFirebase() { }

    @Override
    protected void AttachOnCreateListeners() { }

    @Override
    protected void DestroyOnCreateListeners() { }

    /**
     * {@inheritDoc}
     * This implementation sets the bottom toolbar menu to the server_menu menu resource by default.
     */

    @Override
    protected int setBottomToolbarMenuID() {
        return R.menu.server_menu;
    }

    // ACTIVITY SPECIFIC METHODS

    /**
     * Resets the sharing code of the current Share Server Popup when called, if possible.
     */

    private void resetShareCode() {
        if(popup != null) {
            popup.resetCode();
        }
    }

    /**
     * Hides the Server Settings Menu Item if the current user is not the owner of the current server.
     */

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

    /**
     * Returns the user back to the View Servers activity when called.
     */

    private void returnToViewServers() {
        Intent returnToViewServers = new Intent(currentActivity, ViewServersActivity.class);

        // Clear all server activities on top of the activity.
        returnToViewServers.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(returnToViewServers);
        logHandler.printActivityIntentLog("View Servers Activity");

        resetShareCode();
    }

    /**
     * Initialises and displays the Share Server popup.
     */

    private void showShareServerPopup(ConstraintLayout baseLayer) {
        popup = new ShareServerPopup(baseLayer, currentActivity.getBaseContext(), databaseRef, serverId);
        popup.present();
    }

    /**
     * Deletes a server by unsubscribing all its members, then deleting all linked data.
     * @param _serverId The id of the server to delete.
     */

    private void deleteServer(final String _serverId) {

        final ValueEventListener removeSubscriptionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    if(data.getKey() == null) {
                        logHandler.printDatabaseResultLog(".getChildren().Key()", "Subscribed User ID", "deleteServer", "null");
                        continue;
                    }

                    String userID = data.getKey();
                    logHandler.printDatabaseResultLog(".getChildren().Key()", "Subscribed User ID", "deleteServer", userID);

                    databaseRef.child("users").child(userID).child("_subscribedServers").child(_serverId).setValue(null);
                }

                // DELETE ALL SERVER DATA AFTER MEMBERS ARE GONE
                databaseRef.child("messages").child(_serverId).setValue(null);
                databaseRef.child("members").child(_serverId).setValue(null);
                databaseRef.child("titles").child(_serverId).setValue(null);
                databaseRef.child("posts").child(_serverId).setValue(null);
                databaseRef.child("postmessages").child(_serverId).setValue(null);

                // DELETE ALL POST PHOTOS IN STORAGE
                StorageReference storeRef = FirebaseStorage.getInstance().getReference();
                storeRef.child("posts").child(_serverId).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for(StorageReference fileRef: listResult.getItems()) {
                            fileRef.delete().addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    logHandler.printLogWithMessage("Couldn't delete item!" + e.toString());
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        logHandler.printLogWithMessage("Couldn't retrieve storage items for deletion!");
                    }
                });

                // DELETE SERVER LAST BECAUSE OWNER ID IS REQUIRED FOR RULES
                databaseRef.child("servers").child(_serverId).setValue(null);

                logHandler.printLogWithMessage("Server is completely deleted! Returning user back to View Server Activity!");
                returnToViewServers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        databaseRef.child("members")
                   .child(_serverId)
                   .addListenerForSingleValueEvent(removeSubscriptionsListener);
    }

    /**
     * Contextually handles the Leave Server action.
     */

    private void handleLeaveServerAlert() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        final String currentUID = currentUser.getUid();
        builder.setTitle("Leave Server?");

        if(isOwner) {

            // User is server owner
            builder.setMessage("Are you sure you want to leave the server? As the owner, this will delete the server for everyone as well!");
            builder.setNegativeButton("Yes, leave the server", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    logHandler.printLogWithMessage("Deleting server for all users!");
                    deleteServer(serverId);
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

                    // Send user left server notification in chat
                    Utils.SendUserActionSystemMessage(logHandler, databaseRef, currentUID, " left the server! :(", serverId);

                    final String server = serverId;

                    //Remove Server Chat Notifications for user
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(server).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("UNSUBSCRIBED FROM /topics/" + server +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT UNSUBSCRIBE");
                            }
                        }
                    });

                    //Remove Server System Notifications for user
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("system" + server).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("UNSUBSCRIBED FROM /topics/system" + server +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT UNSUBSCRIBE");
                            }
                        }
                    });

                    //Remove Server Posts Notifications for user
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("posts" + server).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                logHandler.printLogWithMessage("UNSUBSCRIBED FROM /topics/posts" + server +" SUCCESSFULLY");

                            }
                            else{
                                logHandler.printLogWithMessage("COULD NOT UNSUBSCRIBE");
                            }
                        }
                    });

                    //Send user left server notification in push notification
                    sendSystemNotification(serverId, currentUID," has left the server! :(");
                    logHandler.printLogWithMessage("Users in Server notified of leaving member!");


                    logHandler.printLogWithMessage("Returning user back to View Server Activity!");
                    returnToViewServers();
                }
            });

        }

        builder.setNeutralButton("Cancel", null);
        builder.create().show();
        logHandler.printLogWithMessage("Presenting Leave Server Dialog!");
    }

    // PROTECTED CONVENIENCE METHODS

    /**
     * Convenience method for adding default SERVER_ID and IS_OWNER extras into an intent
     * @param intent The intent for which the default extras should be put for.
     */

    protected void PutExtrasForServerIntent(Intent intent) {
        intent.putExtra(SERVER_ID_KEY, serverId);
        intent.putExtra(IS_OWNER_KEY, isOwner);
    }

    /**
     * Increments the experience points of a single server member.
     * @param _userId The id of the member to add experience for.
     * @param _serverId The id of the server that the member is in to add exp for.
     * @param _exp The value of exp to increment.
     * @param _secondsCooldown The number of seconds until which that member can get exp again.
     */

    protected void AddExpForServerMember(final String _userId, final String _serverId, final int _exp, int _secondsCooldown) {
        String PREF_FILE = "cooldownPref";
        String COOLDOWN_KEY = "cooldownFinish";

        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        long millisCooldownFinish = prefs.getLong(COOLDOWN_KEY, -1);
        long millisNow = System.currentTimeMillis();

        if(millisCooldownFinish == -1 || millisNow > millisCooldownFinish) {

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
                    Integer oldLevel = Progression.ConvertExpToLevel(currentExp.intValue());
                    Integer newLevel = Progression.ConvertExpToLevel(currentExp.intValue() + _exp);
                    if(oldLevel < newLevel) {

                        // Announce level up in chat
                        Utils.SendUserActionSystemMessage(logHandler, databaseRef, _userId, " has leveled up! " + oldLevel + " -> " + newLevel, _serverId);
                        sendSystemNotification(serverId,_userId," has leveled up! " + oldLevel + " -> " + newLevel + " !");

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

    /**
     * Startup configuration code for all menu items before further setup is applied.
     */

    private void setupAllMenuItems() {
        if(BottomToolbarAMV != null) {
            ActionMenuItemView posts = (ActionMenuItemView) currentActivity.findViewById(R.id.postsMenuItem);
            ActionMenuItemView chat = (ActionMenuItemView) currentActivity.findViewById(R.id.chatMenuItem);
            ActionMenuItemView members = (ActionMenuItemView) currentActivity.findViewById(R.id.membersMenuItem);
            toggleMenuItem(posts, true);
            toggleMenuItem(chat, true);
            toggleMenuItem(members, true);
            if(isOwner) {
                ActionMenuItemView settings = (ActionMenuItemView) currentActivity.findViewById(R.id.settingsMenuItem);
                toggleMenuItem(settings, true);
            }
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
        resetShareCode();
        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        hideSettingsIfNotOwner();
        setupAllMenuItems();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
