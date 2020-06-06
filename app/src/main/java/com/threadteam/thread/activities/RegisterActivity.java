package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.threadteam.thread.R;
import com.threadteam.thread.models.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText _UserName, _Email, _Password, _CfmPassword;
    private Button  _RegisterBtn;
    private TextView _LoginBtn;
    private FirebaseAuth fAuth;
    private ProgressBar progressBar;
    private DatabaseReference reff;
    private User user;

    private String _aboutUsMessage;
    private String _statusMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        user = new User();
        reff = FirebaseDatabase.getInstance().getReference().child("users");

        _RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = _UserName.getText().toString().trim();
                String email = _Email.getText().toString().trim();
                String password =  _Password.getText().toString().trim();
                String cfmpassword = _CfmPassword.getText().toString().trim();

                closeKeyboard();

                if(TextUtils.isEmpty(username)){
                    _UserName.setError("Username is required");
                    return;
                }

                if(username.length() < 2 ){
                   _UserName.setError("Minimum of 3 characters is required");
                    return;
                }

                if (username.length() > 16){
                    _UserName.setError("Maximum 16 characters only");
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    _Email.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    _Password.setError("Password is required");
                    return;
                }

                if (password.length() < 7){
                    _Password.setError("Minimum of 7 characters required");
                    return;
                }

                if(!password.equals(cfmpassword)){
                    _CfmPassword.setError("Passwords do not match");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                progressBar.getProgress();

                //register user into firebase
                fAuth.createUserWithEmailAndPassword(email,cfmpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            user.set_username(username);
                            user.set_aboutUsMessage(_aboutUsMessage);
                            user.set_statusMessage(_statusMessage);
                            String UserID  = fAuth.getCurrentUser().getUid();

                            reff.child(UserID).setValue(user);
                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),ViewServersActivity.class));

                            finish();
                        }
                        else{
                            Toast.makeText(RegisterActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        _LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
