package com.threadteam.thread.popups;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;

import com.threadteam.thread.LogHandler;
import com.threadteam.thread.libraries.Utils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Handles the Share Server Popup for server activities.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 1.0
 */

public class ShareServerPopup {

    // LOGGING

    /** The popup specific log handler object. */
    private LogHandler logHandler = new LogHandler("Share Server Popup");

    // DATA STORE

    /** The context which the popup was created in. */
    private Context context;

    /** The DatabaseReference of the current activity. */
    private DatabaseReference ref;

    /** The current server share code. */
    private String shareCode = null;

    /** The id of the current server being shared. */
    private String serverId;

    // VIEW OBJECTS

    /** The base layer that the popup should be displayed on. */
    private ViewGroup baseLayer;

    /** The main view of the popup. */
    private View view;

    /** The main window of the popup. */
    private PopupWindow window;

    /** The top toolbar of the popup. */
    private Toolbar PopupToolbar;

    /** Refreshes the server share code when triggered. */
    private Button RefreshCodeButton;

    /** Displays the server share code to the user. */
    private TextView ShareCodeTextView;

    /** Displays hints for how to use the server share code. */
    private TextView ShareCodeDescTextView;

    /** Displays how much time remains until the server share code resets. */
    private TextView CodeExpiryTextView;

    // INITIALISE LISTENERS

    /**
     *  Verifies that there is no identical share code to the current share code in the shares tree.
     *  Reattempts the generation and verification of a new share code should a duplicate be found.
     *  Otherwise, pairs the server share code with the current server id if successful, and updates the view accordingly.
     *
     *  Database Path:      root/shares/(shareCode)
     *  Usage:              Single ValueEventListener
     */

    final ValueEventListener sharesListener = new ValueEventListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            // CHECKS /SHARES/(SHARE_CODE). IF NODE EXISTS, REATTEMPTS CODE GENERATION (COLLISION)
            if (dataSnapshot.getValue() != null) {
                attemptGenerateCode();
                return;
            }

            // IF NO COLLISION, CREATE NEW SHARE CODE NODE
            ref.child("shares").child(shareCode).setValue(serverId);

            // UPDATE VIEW TO REFLECT NEW GENERATED CODE
            ShareCodeTextView.setText(shareCode);
            RefreshCodeButton.setText("REFRESH");
            ShareCodeDescTextView.setText(R.string.share_code_hasCode);
            codeTimeout.start();

            logHandler.printLogWithMessage("Code generated successfully, timer started!");
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    /** Countdown timer for the server share code's expiry */

    final CountDownTimer codeTimeout = new CountDownTimer(10*60*1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            long mins = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
            long secs = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - (mins * 60);
            CodeExpiryTextView.setText(String.format(Locale.ENGLISH, "expires in %02d:%02d", mins, secs));
        }

        @Override
        public void onFinish() {
            logHandler.printLogWithMessage("Timer has ended!");

            resetViewObjects();
            resetCode();
        }
    };

    /**
     * Constructor for the Share Server Popup.
     * @param _baseLayer The base layer that the popup should be displayed on.
     * @param _context The current context.
     * @param _ref The DatabaseReference of the current activity.
     * @param _serverId The id of the current server being shared.
     */

    public ShareServerPopup(ViewGroup _baseLayer, Context _context, DatabaseReference _ref, String _serverId) {
        this.baseLayer = _baseLayer;
        this.context = _context;
        this.ref = _ref;
        this.serverId = _serverId;
    }

    /**
     * Presents the popup on the base layer.
     */

    public void present() {
        createView();
        createWindow();
        bindViewObjects();
        setupViewObjects();
    }

    /**
     * Dismisses the popup and does cleanup.
     */

    public void dismiss() {
        resetCode();
        codeTimeout.cancel();
        window.dismiss();
    }

    /**
     * Resets the server share code to null.
     */

    public void resetCode() {
        logHandler.printLogWithMessage("Resetting Share Code (if not null)!");

        if(shareCode != null) {
            ref.child("shares").child(String.valueOf(shareCode)).setValue(null);
            shareCode = null;
        }
    }

    /**
     * Initialises the popup's view by inflating a layout.
     */

    private void createView() {
        view = LayoutInflater.from(context).inflate(
                R.layout.activity_shareserver, baseLayer, false
        );

        logHandler.printLogWithMessage("Created Popup View!");
    }

    /**
     * Initialises the popup's window and presents it on the base layer.
     */

    private void createWindow() {
        window = new PopupWindow(
                view,
                (int) (baseLayer.getWidth() * 0.8),
                (int) (baseLayer.getHeight() *0.8),
                true
        );

        window.setTouchable(true);
        window.showAtLocation(baseLayer, Gravity.CENTER, 0 ,0);
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dismiss();
            }
        });

        logHandler.printLogWithMessage("Created Popup Window!");
    }

    /**
     * Binds the view objects for the current popup view to their respective objects.
     */

    private void bindViewObjects() {
        PopupToolbar = view.findViewById(R.id.shareCodeToolbar);
        RefreshCodeButton = view.findViewById(R.id.refreshCodeButton);
        ShareCodeTextView = view.findViewById(R.id.shareCodeTextView);
        ShareCodeDescTextView = view.findViewById(R.id.shareCodeDescTextView);
        CodeExpiryTextView = view.findViewById(R.id.codeExpiryTextView);

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);
    }

    /**
     * Sets up the bound view objects and defines their behaviour.
     */

    private void setupViewObjects() {
        PopupToolbar.setTitle("Share Server Code");
        resetViewObjects();

        RefreshCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FOR REFRESH
                resetCode();
                codeTimeout.cancel();
                logHandler.printLogWithMessage("Timer reset!");

                // GET CODE
                attemptGenerateCode();
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_SETUP);
    }

    /**
     * Attempts to generate a code and runs sharesListener to verify if it works.
     */

    private void attemptGenerateCode() {
        shareCode = Utils.GenerateAlphanumericID(6);
        ref.child("shares")
                .child(shareCode)
                .addListenerForSingleValueEvent(sharesListener);
    }

    /**
     * Sets/Resets all bound view objects to the starting configuration.
     */

    @SuppressLint("SetTextI18n")
    private void resetViewObjects() {
        RefreshCodeButton.setText("GET CODE");
        ShareCodeTextView.setText("------");
        ShareCodeDescTextView.setText(R.string.share_code_noCode);
        CodeExpiryTextView.setText("expires in 00:00:00");
    }
}
