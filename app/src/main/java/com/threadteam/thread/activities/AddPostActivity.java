package com.threadteam.thread.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;

// ADD POST ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// DANNY CHAN, S10196363F
//
// DESCRIPTION
// Allows users to create post one at a time
//
// NAVIGATION
// PARENT: VIEW POST
// CHILDREN: N/A
public class AddPostActivity extends AppCompatActivity {

    private LogHandler logHandler = new LogHandler("AddPost Activity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpost);
    }
}