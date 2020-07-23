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

public class ShareServerPopup {

    // LOGGING

    private LogHandler logHandler = new LogHandler("Share Server Popup");

    // DATA STORE

    private Context context;
    private DatabaseReference ref;
    private String shareCode = null;
    private String serverId;

    // VIEW OBJECTS

    private ViewGroup baseLayer;
    private View view;
    private PopupWindow window;

    private Toolbar PopupToolbar;
    private Button RefreshCodeButton;
    private TextView ShareCodeTextView;
    private TextView ShareCodeDescTextView;
    private TextView CodeExpiryTextView;

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

    public ShareServerPopup(ViewGroup _baseLayer, Context _context, DatabaseReference _ref, String _serverId) {
        this.baseLayer = _baseLayer;
        this.context = _context;
        this.ref = _ref;
        this.serverId = _serverId;
    }

    public void present() {
        createView();
        createWindow();
        bindViewObjects();
        setupViewObjects();
    }

    public void dismiss() {
        resetCode();
        codeTimeout.cancel();
        window.dismiss();
    }

    public void resetCode() {
        logHandler.printLogWithMessage("Resetting Share Code (if not null)!");

        if(shareCode != null) {
            ref.child("shares").child(String.valueOf(shareCode)).setValue(null);
            shareCode = null;
        }
    }

    private void createView() {
        view = LayoutInflater.from(context).inflate(
                R.layout.activity_shareserver, baseLayer, false
        );

        logHandler.printLogWithMessage("Created Popup View!");
    }

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

    private void bindViewObjects() {
        PopupToolbar = view.findViewById(R.id.shareCodeToolbar);
        RefreshCodeButton = view.findViewById(R.id.refreshCodeButton);
        ShareCodeTextView = view.findViewById(R.id.shareCodeTextView);
        ShareCodeDescTextView = view.findViewById(R.id.shareCodeDescTextView);
        CodeExpiryTextView = view.findViewById(R.id.codeExpiryTextView);

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);
    }

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

    private void attemptGenerateCode() {
        shareCode = Utils.GenerateAlphanumericID(6);
        ref.child("shares")
                .child(shareCode)
                .addListenerForSingleValueEvent(sharesListener);
    }

    @SuppressLint("SetTextI18n")
    private void resetViewObjects() {
        RefreshCodeButton.setText("GET CODE");
        ShareCodeTextView.setText("------");
        ShareCodeDescTextView.setText(R.string.share_code_noCode);
        CodeExpiryTextView.setText("expires in 00:00:00");
    }
}
