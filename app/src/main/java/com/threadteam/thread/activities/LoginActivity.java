package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.libraries.Notifications;


/**
 * This activity class handles the logging in and resetting password of user.
 *
 * @author Mohamed Thabith
 * @version 2.0
 * @since 1.0
 */


public class LoginActivity extends AppCompatActivity {



    // LOGGING
    private LogHandler logHandler = new LogHandler("Login Activity");


    // FIREBASE
    /** Firebase Authentication instance for the current session. */
    private FirebaseAuth fAuth;

    /** Firebase Database Reference for the current session. */
    private DatabaseReference reff, databaseRef;

    // VIEW OBJECTS
    /** EditText
     *
     * _EmailLog        Contains email of user to be retrieved from database.
     * _Password        Contains password of user to be retrieved from database.
     * _EmailReset      Contains of email of user to be reset. */
    private EditText _EmailLog, _Password, _EmailReset;

    /** Button
     *
     * _LoginBtn        Triggers logging into app.
     * _ResetBtn        Triggers resetting of password. */
    private Button _LoginBtn, _ResetBtn;

    /** TextView
     *
     * _RegisterBtn     Triggers register card view.
     * _ForgotBtn       Triggers reset password card view.
     * _CancelBtn       Triggers login card view. */
    private TextView _RegisterBtn, _ForgotPwBtn, _CancelBtn;

    /** Displays log in progress when _LoginBtn is clicked*/
    private ProgressBar progressBar;

    /** CardView
     *
     * _LoginCard       Displays login card view, login fields and buttons.
     * _ResetCard       Displays reset card view, reset fields and buttons. */
    private CardView _LoginCard, _ResetCard;

    /** Nested scroll view of page. */
    private NestedScrollView  _LOGINVIEW;


    // ACTIVITY STATE MANAGEMENT METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logHandler.printDefaultLog(LogHandler.STATE_ON_CREATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //BIND VIEW OBJECTS
        _LOGINVIEW = findViewById(R.id.loginVIEW);

        _LoginCard = findViewById(R.id.cardView);
        _EmailLog = findViewById(R.id.emailLog);
        _Password = findViewById(R.id.passwordLog);
        _LoginBtn = findViewById(R.id.loginBtn);
        _RegisterBtn = findViewById(R.id.regAcc);
        _ForgotPwBtn = findViewById(R.id.fgtPw);

        _ResetCard = findViewById((R.id.resetCard));
        _EmailReset = findViewById(R.id.emailReset);
        _ResetBtn = findViewById(R.id.resetBtn);
        _CancelBtn = findViewById(R.id.cancelBtn);
        progressBar = findViewById(R.id.progressBarLogin);

        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);

        // INITIALISE FIREBASE
        fAuth = FirebaseAuth.getInstance();
        reff = FirebaseDatabase.getInstance().getReference().child("users");
        databaseRef = FirebaseDatabase.getInstance().getReference();
        final String _token = FirebaseInstanceId.getInstance().getToken();

        //Validation for if there is a user no login required
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), ViewServersActivity.class));
            finish();
        }
        logHandler.printDefaultLog(LogHandler.FIREBASE_USER_FOUND);

        // SETUP VIEW OBJECTS
        //Populate Buttons with Listeners
        _LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Close keyboard upon button press
                closeKeyboard();
                String emailLog = _EmailLog.getText().toString().trim();
                String password = _Password.getText().toString().trim();

                //Validation if Email Field is empty
                if (TextUtils.isEmpty(emailLog)) {
                    _EmailLog.setError("Email is required");
                    logHandler.printLogWithMessage("No Email Inputted");

                    return;
                }

                //Validation if Password Field is empty
                if (TextUtils.isEmpty(password)) {
                    _Password.setError("Password is required");
                    logHandler.printLogWithMessage("No Password Inputted");

                    return;
                }

                //Set Progressbar to visible
                progressBar.setVisibility(View.VISIBLE);

                //authenticate User
                fAuth.signInWithEmailAndPassword(emailLog, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       // if successful add token, show toast, set notifications and move to ViewServerActivity
                        if (task.isSuccessful()) {

                            //Get UserID
                            String UserID = fAuth.getCurrentUser().getUid();
                            //Set token under user in database
                            reff.child(UserID).child("_token").setValue(_token);

                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            logHandler.printLogWithMessage("Logged in Successfully");

                            /**
                             * Retrieves the current notification data of the current user.
                             *
                             *  Database Path:      root/users/(UserID)/notifications
                             *  Usage:              ValueEventListener
                             */
                            ValueEventListener getNotificationSettings = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.getValue() == null) {
                                        logHandler.printDatabaseResultLog(".getValue()", "Notifications Values", "getNotificationSettings", "null");
                                        reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_msg").setValue("on");
                                        reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_system").setValue("on");
                                        reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_post").setValue("on");
                                        return;
                                    }

                                    String messageNotification = (String) dataSnapshot.child("_msg").getValue();

                                    String systemNotification = (String) dataSnapshot.child("_system").getValue();

                                    String  postNotification = (String) dataSnapshot.child("_post").getValue();

                                    if(messageNotification == null) {
                                        logHandler.printDatabaseResultLog(".child(\"_msg\").getValue()", "messageNotification", "getNotificationSettings", "null");
                                        reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_msg").setValue("on");

                                        return;
                                    }
                                    else if(systemNotification == null) {
                                        logHandler.printDatabaseResultLog(".child(\"_system\").getValue()", "systemNotification", "getNotificationSettings", "null");
                                        reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_system").setValue("on");
                                        return;
                                    }
                                    else if(postNotification == null) {
                                        logHandler.printDatabaseResultLog(".child(\"_post\").getValue()", "postNotification", "getNotificationSettings", "null");
                                        reff.child(fAuth.getCurrentUser().getUid()).child("_notifications").child("_post").setValue("on");

                                        return;
                                    }

                                    if(messageNotification.equals("on")){
                                        Notifications.subscribeMsgNotification(logHandler,databaseRef);
                                    }
                                    if(systemNotification.equals("on")){
                                        Notifications.subscribeSystemNotification(logHandler,databaseRef);
                                    }
                                    if(postNotification.equals("on")){
                                        Notifications.subscribePostsNotification(logHandler,databaseRef);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };
                            databaseRef.child("users")
                                    .child(UserID)
                                    .child("_notifications")
                                    .addValueEventListener(getNotificationSettings);

                            //NAVIGATE TO VIEW SERVER ACTIVITY
                            startActivity(new Intent(getApplicationContext(), ViewServersActivity.class));
                            logHandler.printActivityIntentLog("View Server Activity");

                            finish();

                        // if unsuccessful show toast and try again
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid Email or Password ", Toast.LENGTH_SHORT).show();
                            logHandler.printLogWithMessage("Login Unsuccessful");

                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        _RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Navigating to Register Activity
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                logHandler.printLogWithMessage("User tapped on register button!");
                logHandler.printActivityIntentLog("Register Activity");

            }
        });

        _ForgotPwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Resetting Password

                //_LoginCard set to invisible, _ResetCard visible
                _LoginCard.setVisibility(View.INVISIBLE);
                logHandler.printLogWithMessage("User tapped on forgot password button!");
            }
        });

        _ResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TO RESET PASSWORD

                String emailReset = _EmailReset.getText().toString().trim();

                //DIALOG BOX AFTER SUCCESSFUL EMAIL RESET
                final AlertDialog.Builder resetpw = new AlertDialog.Builder(v.getContext());
                resetpw.setMessage("The Reset Link has been sent to your Email");

                //PRESSING OK WILL SET _LoginCard back to visible, blocking _ResetCard
                resetpw.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logHandler.printLogWithMessage("User selected OK");

                        //_LoginCard set to visible, _ResetCard hidden
                        _LoginCard.setVisibility(View.VISIBLE);

                    }
                });

                //Validation if Password Field is empty
                if (TextUtils.isEmpty(emailReset)) {
                    _EmailReset.setError("Email is required");
                    logHandler.printLogWithMessage("No Email Inputted");

                    return;
                }

                fAuth.sendPasswordResetEmail(emailReset).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            logHandler.printLogWithMessage("Reset Email has been successfully sent");
                            //DialogBox shown
                            resetpw.create().show();
                            logHandler.printLogWithMessage("DialogBoxShown");
                            //Keyboard is closed
                            closeKeyboard();

                        } else {
                            Toast.makeText(LoginActivity.this,"Invalid Email", Toast.LENGTH_SHORT).show();
                            logHandler.printLogWithMessage("Invalid Email Inputted");

                        }

                    }
                });



            }
        });


        _CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cancelling reset Password function
                logHandler.printLogWithMessage("Reset Password Cancelled");

                //_LoginCard Set to Visible, hiding _ResetCard
                _LoginCard.setVisibility(View.VISIBLE);


            }
        });





    }

    /**
     * Closes keyboard
     * */

    //Close Keyboard
    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            logHandler.printLogWithMessage("Keyboard Hidden");

        }
    }
}