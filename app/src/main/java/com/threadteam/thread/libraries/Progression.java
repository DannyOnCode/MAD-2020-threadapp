package com.threadteam.thread.libraries;

import android.graphics.Color;

import androidx.annotation.Nullable;

public class Progression {

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

    public static Integer ConvertLevelToStage(int level) {
        if(level < 40) {
            return level / 5;
        } else if(level < 50) {
            return 8;
        } else {
            return 9;
        }
    }

    public static Integer ConvertStageToMinLevel(int stage) {
        if(stage < 8) {
            return stage*5;
        } else {
            return (stage-8) * 10 + 40;
        }
    }

    public static @Nullable
    String GetDefaultTitleForStage(int stage) {
        switch (stage) {
            case 0:
                return "Wanderer";
            case 1:
                return "Visitor";
            case 2:
                return "Acquaintance";
            case 3:
                return "Known";
            case 4:
                return "Friend";
            case 5:
                return "Invested";
            case 6:
                return "Committed";
            case 7:
                return "Legend";
            case 8:
                return "Elder";
            case 9:
                return "Immortal";
            default:
                return null;
        }
    }

    public static @Nullable Integer GetDefaultColorIntForStage(int stage) {
        switch (stage) {
            case 0:
                return Color.rgb(160,160,160);
            case 1:
                return Color.rgb(96,96,96);
            case 2:
            case 3:
                return Color.rgb(0, 0, 0);
            case 4:
            case 5:
                return Color.rgb(0, 0, 255);
            case 6:
                return Color.rgb(0, 128, 0);
            case 7:
                return Color.rgb(255, 128, 0);
            case 8:
                return Color.rgb(127, 0, 255);
            case 9:
                return Color.rgb(255, 0, 0);
            default:
                return null;
        }
    }

}
