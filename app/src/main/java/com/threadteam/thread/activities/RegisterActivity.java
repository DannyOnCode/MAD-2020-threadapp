package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.threadteam.thread.R;

public class RegisterActivity extends AppCompatActivity {
    EditText mUserName, mEmail, mPassword, mCfmPassword;
    Button  mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mUserName = findViewById(R.id.userName);
        mEmail = findViewById(R.id.emailReg);
        mPassword = findViewById(R.id.passwordReg);
        mCfmPassword = findViewById(R.id.cfmPassword);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.existAcc);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBarReg);

        //if(fAuth.getCurrentUser() != null){
        //    startActivity(new Intent(getApplicationContext(),ViewServersActivity.class));
        //    finish();
        //}

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUserName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password =  mPassword.getText().toString().trim();
                String cfmpassword = mCfmPassword.getText().toString().trim();

                if(TextUtils.isEmpty(username)){
                    mUserName.setError("Username is required");
                    return;
                }

                if(username.length() < 2 ){
                   mUserName.setError("Minimum of 3 characters is required");
                    return;
                }

                if (username.length() > 16){
                    mUserName.setError("Maximum 16 characters only");
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is required");
                    return;
                }

                if (password.length() < 7){
                    mPassword.setError("Minimum of 8 characters required");
                    return;
                }

                if(!password.equals(cfmpassword)){
                    mCfmPassword.setError("Passwords do not match");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //register user into firebase
                fAuth.createUserWithEmailAndPassword(email,cfmpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
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

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

    }
}
