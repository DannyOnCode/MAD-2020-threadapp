package com.threadteam.thread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.core.content.ContextCompat;

import com.threadteam.thread.activities.ChatActivity;
import com.threadteam.thread.activities.PostsActivity;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static void StartActivityOnNewStack(Context from, Class<?> target, String targetActivityName, HashMap<String, String> extraMap, LogHandler logHandler) {
        Intent goToChat = new Intent(from, target);
        goToChat.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if(extraMap != null) {
            for (String key : extraMap.keySet()) {
                goToChat.putExtra(key, extraMap.get(key));
            }
        }

        from.startActivity(goToChat);
        logHandler.printActivityIntentLog(targetActivityName);

        if(extraMap != null) {
            for (String key : extraMap.keySet()) {
                logHandler.printIntentExtrasLog(key, extraMap.get(key) != null ? extraMap.get(key) : "");
            }
        }
    }

    // NAME:                ToggleMenuItemAlpha
    // DESCRIPTION:         TOGGLES THE SPECIFIED MENU ITEM'S VISUAL STATE FOR THE CURRENT ACTIVITY
    // INPUTS:
    // parentActivity:      THE CURRENT ACTIVITY
    // itemId:              THE ID OF THE MENU ITEM
    // itemName:            THE NAME OF THE MENU ITEM
    // drawableId:          THE ID OF THE DRAWABLE TO BE USED FOR THE MENU ITEM
    // drawableName:        THE NAME OF THE DRAWABLE TO BE USED FOR THE MENU ITEM
    // enabled:             WHEN TRUE, SETS THE MENU ITEM TO FULL OPACITY, OTHERWISE SETS IT TO 40%
    // logHandler:          THE LOG HANDLER FOR THE CURRENT ACTIVITY
    // RETURN VALUE:        NULL

    @SuppressLint("RestrictedApi")
    public static void ToggleMenuItemAlpha(AppCompatActivity currentActivity, int itemId, String itemName, int drawableId, String drawableName, Boolean enabled, LogHandler logHandler) {
        ActionMenuItemView ViewServersAMIV = (ActionMenuItemView) currentActivity.findViewById(itemId);

        if(ViewServersAMIV == null) {
            logHandler.printLogWithMessage("Can't find Bottom Toolbar menu item for " + itemName + "! Cancelling icon update!");
            return;
        }

        Drawable drawable = ContextCompat.getDrawable(currentActivity, drawableId);

        if(drawable == null) {
            logHandler.printLogWithMessage("Drawable for " + drawableName + " not found! Cancelling icon update!");
        } else {
            if(enabled) {
                drawable.setColorFilter(null);
            } else {
                drawable.setColorFilter(Color.argb(40, 255, 255, 255), PorterDuff.Mode.MULTIPLY);
            }
            ViewServersAMIV.setIcon(drawable);

            logHandler.printLogWithMessage("Successfully toggled menu item for " + itemName + " to " + enabled.toString());
        }
    }

    public static Integer ConvertExpToLevel(int exp) {
        return (int) Math.floor(1.26 * Math.sqrt(exp)) + 1;
    }

    public static Integer GetExpToNextLevel(int level) {
        return (int) Math.ceil(Math.pow(level/1.26, 2));
    }

    public static Integer GetExpProgress(int exp, int level) {
        double currentExpRange = GetExpToNextLevel(level) - GetExpToNextLevel(level-1);
        double currentExp = exp - GetExpToNextLevel(level-1);
        return (int) (currentExp / currentExpRange * 100);
    }

}
