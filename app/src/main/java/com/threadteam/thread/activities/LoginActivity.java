package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
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
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;


// LOGIN ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// MOHAMED THABITH, S10196396B
//
// DESCRIPTION
// Handles logging in of user
// Handles resetting password of user
//
// NAVIGATION
// PARENT: LOGIN ACTIVITY
// CHILDREN: NONE
// OTHER: VIEW SERVER



public class LoginActivity extends AppCompatActivity {



    // LOGGING
    private LogHandler logHandler = new LogHandler("Login Activity");


    // FIREBASE
    //
    //    // fAuth:            FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION
    private FirebaseAuth fAuth;

    // VIEW OBJECTS
    //
    // _EmailLog:               CONTAINS EMAIL OF USER TO BE RETRIEVED FROM DATABASE
    // _Password    :               CONTAINS PASSWORD OF USER TO BE RETRIEVED FROM DATABASE
    // _EmailReset:             CONTAINS EMAIL OF USER TO BE RESET
    // _LoginBtn:               TRIGGERS LOGGING IN TO APP
    // _ResetBtn                TRIGGERS RESETTING PASSWORD
    // _RegisterBtn:            TRIGGERS RESET CARD VIEW
    // _ForgotPwBtn:            TRIGGERS RESET PASSWORD
    // _CancelBtn:              TRIGGERS RETURN TO LOGIN CARD VIEW
    // progressBar              DISPLAYS LOG IN PROGRESS ONCE _LoginBtn HAS BEEN CLICKED
    // _LoginCard               DISPLAY LOGIN FIELDS AND BUTTONS
    // _ResetCard               DISPLAY RESET PASSWORD FIELDS AND BUTTONS
    // _LOGINVIEW               NESTED SCROLL VIEW OF PAGE



    private EditText _EmailLog, _Password, _EmailReset;
    private Button _LoginBtn, _ResetBtn;
    private TextView _RegisterBtn, _ForgotPwBtn, _CancelBtn;
    private ProgressBar progressBar;
    private CardView _LoginCard, _ResetCard;
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
                       // if successful show toast and move to ViewServerActivity
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            logHandler.printLogWithMessage("Logged in Successfully");

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
                logHandler.printActivityIntentLog("Register Activity");

            }
        });

        _ForgotPwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Resetting Password

                //_LoginCard set to invisible, _ResetCard visible
                _LoginCard.setVisibility(View.INVISIBLE);
                logHandler.printLogWithMessage("Reset Password Card Opened");
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