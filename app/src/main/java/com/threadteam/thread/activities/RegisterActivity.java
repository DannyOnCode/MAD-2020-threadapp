package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import android.app.Activity;
import android.content.Context;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.models.User;
import com.threadteam.thread.notifications.Token;



// REGISTER ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// MOHAMED THABITH, S10196396B
//
// DESCRIPTION
// Handles REGISTERING OF USER
//
//
// NAVIGATION
// PARENT: NONE
// CHILDREN: LOGIN ACTIVITY
// OTHER: VIEW SERVER


public class RegisterActivity extends AppCompatActivity {

    // LOGGING
    private LogHandler logHandler = new LogHandler("Register Activity");

    // FIREBASE
    //
    // fAuth:                FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION.
    // reff:                 FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION.

    private FirebaseAuth fAuth;
    private DatabaseReference reff;
    private User user;

    // VIEW OBJECTS
    //
    // _Username                CONTAINS USERNAME OF USER TO BE UPLOADED TO DATABASE
    // _Email:                  CONTAINS EMAIL OF USER TO BE UPLOADED TO DATABASE
    // _Password:               CONTAINS PASSWORD OF USER TO BE UPLOADED TO DATABASE
    // _CfmPassword             CONTAINS CONFIRM PASSWORD OF USER TO BE CHECKED WITH _Password
    // _RegisterBtn:            TRIGGERS REGISTERING INTO DATABASE AND LOGGING IN TO APP
    // _LoginBtn:               TRIGGERS RETURN TO LOGIN CARD VIEW
    // progressBar              DISPLAYS LOG IN PROGRESS ONCE _LoginBtn HAS BEEN CLICKED
    // _REGISTERVIEW            NESTED SCROLL VIEW OF PAGE



    private EditText _UserName, _Email, _Password, _CfmPassword;
    private Button  _RegisterBtn;
    private TextView _LoginBtn;
    private ProgressBar progressBar;
    private NestedScrollView _REGISTERVIEW;


    //_aboutUsMessage           ABOUT MESSAGE IN ViewProfileActivity
    //_statusMessage            STATUS MESSAGE IN ViewProfileActivity

    private String _aboutUsMessage;
    private String _statusMessage;
    // ACTIVITY STATE MANAGEMENT METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logHandler.printDefaultLog(LogHandler.STATE_ON_CREATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //BIND VIEW OBJECTS

        _REGISTERVIEW = findViewById(R.id.registerVIEW);

        _UserName = findViewById(R.id.userNameReg);
        _Email = findViewById(R.id.emailReg);
        _Password = findViewById(R.id.passwordReg);
        _CfmPassword = findViewById(R.id.cfmPassword);
        _RegisterBtn = findViewById(R.id.registerBtn);
        _LoginBtn = findViewById(R.id.existAcc);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBarReg);

        _aboutUsMessage = "No Description";
        _statusMessage = "No Status";

        // INITIALISE FIREBASE
        user = new User();
        reff = FirebaseDatabase.getInstance().getReference().child("users");
        String token = FirebaseInstanceId.getInstance().getToken();
        final Token _token = new Token(token);



        // SETUP VIEW OBJECTS
        //Populate Buttons with Listeners
        _RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = _UserName.getText().toString().trim();
                String email = _Email.getText().toString().trim();
                String password =  _Password.getText().toString().trim();
                String cfmpassword = _CfmPassword.getText().toString().trim();

                //Close keyboard upon button press
                closeKeyboard();

                //Validation if Username Field is empty
                if(TextUtils.isEmpty(username)){
                    _UserName.setError("Username is required");
                    logHandler.printLogWithMessage("No Username Inputted");
                    return;
                }

                //Validation if Username Field has more than 2 characters
                if(username.length() < 2 ){
                   _UserName.setError("Minimum of 3 characters is required");
                    logHandler.printLogWithMessage("Less than 3 characters in Username");

                    return;
                }

                //Validation if Email Field is empty
                if(TextUtils.isEmpty(email)){
                    _Email.setError("Email is required");
                    logHandler.printLogWithMessage("No Email Inputted");

                    return;
                }


                //Validation if Password Field is empty
                if(TextUtils.isEmpty(password)){
                    _Password.setError("Password is required");
                    logHandler.printLogWithMessage("No Password Inputted");

                    return;
                }

                //Validation if Password Field more tha 7 characters
                if (password.length() < 7){
                    _Password.setError("Minimum of 7 characters required");
                    logHandler.printLogWithMessage("Less than 7 characters in Password");

                    return;
                }

                //Validation if Password Field and Confirm Password field match

                if(!password.equals(cfmpassword)){
                    _CfmPassword.setError("Passwords do not match");
                    logHandler.printLogWithMessage("Passwords do not match");

                    return;
                }

                //Set Progressbar to visible
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getProgress();

                //register user into firebase
                fAuth.createUserWithEmailAndPassword(email,cfmpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // if successful show toast, register User and move to ViewServerActivity

                        if(task.isSuccessful()){
                            //ADD VALUES TO DATABASE
                            user.set_username(username);
                            user.set_aboutUsMessage(_aboutUsMessage);
                            user.set_statusMessage(_statusMessage);
                            user.set_token(_token);
                            String UserID  = fAuth.getCurrentUser().getUid();
                            reff.child(UserID).setValue(user);

                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            logHandler.printLogWithMessage("Registered Successfully");

                            //NAVIGATE TO VIEW SERVER ACTIVITY
                            startActivity(new Intent(getApplicationContext(),ViewServersActivity.class));
                            logHandler.printActivityIntentLog("View Server Activity");


                            finish();
                        }

                        // if unsuccessful show toast and try again
                        else{
                            Toast.makeText(RegisterActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            logHandler.printLogWithMessage("Registration Unsuccessful");

                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        _LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigating to Login Activity

                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                logHandler.printActivityIntentLog("Login Activity");

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
