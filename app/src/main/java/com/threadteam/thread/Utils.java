package com.threadteam.thread;

import java.util.ArrayList;
import java.util.List;
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

}
