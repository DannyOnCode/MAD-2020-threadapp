package com.threadteam.thread.libraries;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.LogHandler;

import java.util.HashMap;
import java.util.Random;

// UTILS CONVENIENCE CLASS
//
// PROGRAMMER-IN-CHARGE:
// N/A
//
// DESCRIPTION
// CONTAINS CONVENIENCE METHODS THAT SHOULD BE
// AVAILABLE APPLICATION WIDE. EACH METHOD HAS
// DIFFERENT IN-CHARGES.

public class Utils {

    // NAME:                GenerateAlphanumericID
    // IN-CHARGE:           EUGENE LONG, S10193060J
    // DESCRIPTION:         GENERATES A RANDOM ALPHANUMERIC IDENTIFIER STRING length CHARACTERS LONG
    // INPUTS:
    // length:              INTEGER SPECIFYING LENGTH OF THE ALPHANUMERIC STRING
    // RETURN VALUE:        A STRING THAT IS length CHARACTERS LONG

    public static String GenerateAlphanumericID(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = upper.toLowerCase();
        String num = "0123456789";

        char[] charArr = (num + lower + upper).toCharArray();
        StringBuilder result = new StringBuilder();

        Random random = new Random();
        for(int i=0; i<length; i++) {
            result.append(charArr[random.nextInt(charArr.length)]);
        }

        return result.toString();
    }

    public static void SendSystemMessageInChat(@NonNull DatabaseReference databaseRef, @NonNull String message, @NonNull String _serverId) {
        HashMap<String, Object> chatMessageHashMap = new HashMap<>();
        chatMessageHashMap.put("_senderUID", "SYSTEM");
        chatMessageHashMap.put("_sender", "SYSTEM");
        chatMessageHashMap.put("_message", message);
        chatMessageHashMap.put("timestamp", System.currentTimeMillis());
        databaseRef.child("messages").child(_serverId).push().setValue(chatMessageHashMap);
    }

    public static void SendUserActionSystemMessage(final LogHandler logHandler,
                                                   final DatabaseReference databaseRef,
                                                   @NonNull String _userID,
                                                   final String message,
                                                   final String _serverID) {

        // sendUActionMessage:      ATTEMPTS TO SEND A SERVER MESSAGE WITH THE USER'S USERNAME
        //                          CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                              .child(_userID)
        //                                                              .child("_username")
        //                                                              .addListenerForSingleValueEvent(sendUActionMessage);
        //                          SHOULD NOT BE USED INDEPENDENTLY.

        ValueEventListener sendUActionMessage = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = (String) dataSnapshot.getValue();
                if(username == null) {
                    logHandler.printDatabaseResultLog(".getValue()", "Username", "sendJoinMessage", "null");
                    return;
                }

                Utils.SendSystemMessageInChat(databaseRef, username + message, _serverID);
                

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                logHandler.printDatabaseErrorLog(databaseError);
            }
        };

        databaseRef.child("users")
                .child(_userID)
                .child("_username")
                .addListenerForSingleValueEvent(sendUActionMessage);
    }

}
