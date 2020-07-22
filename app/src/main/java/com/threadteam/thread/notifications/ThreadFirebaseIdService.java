package com.threadteam.thread.notifications;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.threadteam.thread.LogHandler;

public class ThreadFirebaseIdService extends FirebaseInstanceIdService {

    private LogHandler logHandler = new LogHandler("Token Activity");

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        if (firebaseUser != null){
            Log.d("myfirebaseid","Refreshed token: " + refreshToken);
            updateToken(refreshToken);
        }

    }

    private void updateToken(String refreshToken) {
        String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(UserID).child("_tokens").setValue(refreshToken);
    }
}
