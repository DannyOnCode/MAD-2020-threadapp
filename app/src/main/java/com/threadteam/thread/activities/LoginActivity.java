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
import com.threadteam.thread.R;

public class LoginActivity extends AppCompatActivity {
    private EditText _Email, _Password;
    private Button _LoginBtn;
    private TextView _RegisterBtn;
    private FirebaseAuth fAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _Email = findViewById(R.id.emailLog);
        _Password = findViewById(R.id.passwordLog);
        _LoginBtn = findViewById(R.id.loginBtn);
        _RegisterBtn = findViewById(R.id.regAcc);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBarLogin);

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), ViewServersActivity.class));
            finish();
        }

        _LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                String email = _Email.getText().toString().trim();
                String password = _Password.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    _Email.setError("Email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    _Password.setError("Password is required");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate User
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), ViewServersActivity.class));
                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid Email or Password ", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });


        _RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
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