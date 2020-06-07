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
import com.threadteam.thread.R;

public class LoginActivity extends AppCompatActivity {
    private EditText _EmailLog, _Password, _EmailReset;
    private Button _LoginBtn, _ResetBtn;
    private TextView _RegisterBtn, _ForgotPwBtn, _CancelBtn;
    private FirebaseAuth fAuth;
    private ProgressBar progressBar;
    private CardView _LoginCard, _ResetCard;
    private NestedScrollView  _LOGINVIEW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                String emailLog = _EmailLog.getText().toString().trim();
                String password = _Password.getText().toString().trim();

                if (TextUtils.isEmpty(emailLog)) {
                    _EmailLog.setError("Email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    _Password.setError("Password is required");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate User
                fAuth.signInWithEmailAndPassword(emailLog, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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

        _ForgotPwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _LoginCard.setVisibility(View.INVISIBLE);
            }
        });

        _ResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailReset = _EmailReset.getText().toString().trim();

                final AlertDialog.Builder resetpw = new AlertDialog.Builder(v.getContext());
                resetpw.setMessage("The Reset Link has been sent to your Email");

                resetpw.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _LoginCard.setVisibility(View.VISIBLE);
                    }
                });

                if (TextUtils.isEmpty(emailReset)) {
                    _EmailReset.setError("Email is required");
                    return;
                }

                fAuth.sendPasswordResetEmail(emailReset).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            resetpw.create().show();
                            closeKeyboard();

                        } else {
                            Toast.makeText(LoginActivity.this,"Invalid Email", Toast.LENGTH_SHORT).show();
                        }

                    }
                });



            }
        });

        _CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _LoginCard.setVisibility(View.VISIBLE);

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