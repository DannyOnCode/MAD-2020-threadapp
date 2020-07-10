package com.threadteam.thread.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.Utils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ServerBaseActivity extends AppCompatActivity {

    // BASE ACTIVITY LOGGING
    private LogHandler logHandler = new LogHandler("Server Base Activity");

    // FIREBASE
    //
    // databaseRef:             FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION

    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    // DATA STORE
    //
    // SHARE_SERVER_MENU_ITEM:  CONSTANT DECLARING ID FOR THE SHARE SERVER MENU ITEM.
    // LEAVE_SERVER_MENU_ITEM:  CONSTANT DECLARING ID FOR THE LEAVE SERVER MENU ITEM.
    // shareCode:               CONTAINS THE CURRENT SHARING CODE OF THE SERVER (IF IT EXISTS)
    //                          ALSO WORKS AS A FLAG FOR THE resetShareCode FUNCTION.
    // isOwner:                 CONTAINS DATA ON WHETHER THE CURRENT USER IS THE OWNER OF THE SERVER

    protected static final int SHARE_SERVER_MENU_ITEM = -1;
    protected static final int LEAVE_SERVER_MENU_ITEM = -2;
    private String shareCode = null;
    protected Boolean isOwner;

    protected void addExpForServerMember(final String userId, final String serverId, final int exp, int secondsCooldown) {
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
                            .child(userId)
                            .child("_subscribedServers")
                            .child(serverId)
                            .setValue(currentExp + exp);

                    // Feedback to user on level up
                    Integer oldLevel = Utils.ConvertExpToLevel(currentExp.intValue());
                    Integer newLevel = Utils.ConvertExpToLevel(currentExp.intValue() + exp);
                    if(oldLevel < newLevel) {
                        //TODO: Maybe make level up dialog nicer? Will have to see if we've got time to implement this
                        Toast.makeText(getApplicationContext(), "You leveled up! Current level: " + newLevel.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    logHandler.printDatabaseErrorLog(databaseError);
                }
            };

            databaseRef.child("users")
                       .child(userId)
                       .child("_subscribedServers")
                       .child(serverId)
                       .addListenerForSingleValueEvent(updateExpListener);

            // Write Cooldown time to sharedprefs
            SharedPreferences.Editor editor = prefs.edit();
            millisCooldownFinish = System.currentTimeMillis() + secondsCooldown * 1000;
            editor.putLong(COOLDOWN_KEY, millisCooldownFinish);
            editor.apply();
        }
    }

    protected void resetShareCode() {
        logHandler.printLogWithMessage("Resetting Share Code (if not null)!");

        if(shareCode != null) {
            databaseRef.child("shares").child(String.valueOf(shareCode)).setValue(null);
            shareCode = null;
        }
    }

    protected void showShareServerPopup(ConstraintLayout baseLayer, final String serverId) {
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

    protected void returnToViewServers() {
        Intent returnToViewServers = new Intent(ServerBaseActivity.this, ViewServersActivity.class);
        startActivity(returnToViewServers);
        logHandler.printActivityIntentLog("ViewServers Activity");

        resetShareCode();
    }

    protected void handleLeaveServerAlert(Context currentContext, final String serverId, final String currentUID) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(currentContext);
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

    protected void addServerMenuItemsToMenu(Menu menu) {
        menu.add(Menu.NONE, SHARE_SERVER_MENU_ITEM, Menu.NONE, "Share Server");
        menu.add(Menu.NONE, LEAVE_SERVER_MENU_ITEM, Menu.NONE, "Leave Server");
    }

    protected void getIsOwner(Intent dataReceiver) {
        String isOwnerString = dataReceiver.getStringExtra("IS_OWNER");

        if (isOwnerString == null) {
            logHandler.printGetExtrasResultLog("IS_OWNER", "null");
        } else {
            logHandler.printGetExtrasResultLog("IS_OWNER", isOwnerString);
            isOwner = isOwnerString.equals(((Boolean) true).toString());
        }
    }

    protected void enableSettingsIfOwner(AppCompatActivity currentActivity) {
        ActionMenuItemView SettingsAMIV = (ActionMenuItemView) currentActivity.findViewById(R.id.settingsMenuItem);

        if(SettingsAMIV == null) {
            logHandler.printLogWithMessage("Can't find Bottom Toolbar menu item for " + "Settings" + "! Cancelling icon update!");
            return;
        }
        SettingsAMIV.setVisibility(View.INVISIBLE);

        if(isOwner){
            SettingsAMIV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        resetShareCode();
        super.onStop();
    }
}
