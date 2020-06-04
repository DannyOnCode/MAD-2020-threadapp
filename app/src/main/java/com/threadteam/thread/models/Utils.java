package com.threadteam.thread.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

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

}
