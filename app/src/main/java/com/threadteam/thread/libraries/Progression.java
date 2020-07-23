package com.threadteam.thread.libraries;

import android.graphics.Color;

import androidx.annotation.Nullable;

/**
 * Wrapper for progression related functions. (Exp, Levels, Stages)
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 */

public class Progression {

    /**
     * Converts experience points into an equivalent amount of levels.
     * @param exp The experience points of a user.
     * @return The level the user is at.
     */

    public static Integer ConvertExpToLevel(int exp) {
        return (int) Math.floor(1.26 * Math.sqrt(exp)) + 1;
    }

    /**
     * Calculates the amount of experience required to get to the next level.
     * @param level The current level.
     * @return The experience required to get to the next level.
     */

    public static Integer GetExpToNextLevel(int level) {
        return (int) Math.ceil(Math.pow(level/1.26, 2));
    }

    /**
     * Calculates the percentage of progress to the next level (out of 100)
     * @param exp The current amount of exp.
     * @param level The current level.
     * @return The progress percentage to the next level.
     */

    public static Integer GetExpProgress(int exp, int level) {
        double currentExpRange = GetExpToNextLevel(level) - GetExpToNextLevel(level-1);
        double currentExp = exp - GetExpToNextLevel(level-1);
        return (int) (currentExp / currentExpRange * 100);
    }

    /**
     * Converts levels into the stage which the level lies in (like a tier).
     * @param level The current level.
     * @return The current stage.
     */

    public static Integer ConvertLevelToStage(int level) {
        if(level < 40) {
            return level / 5;
        } else if(level < 50) {
            return 8;
        } else {
            return 9;
        }
    }

    /**
     * Gets the minimum level requirement for a stage.
     * @param stage The stage for which the minimum level is to be calculated.
     * @return The minimum level to get to the specified stage.
     */

    public static Integer ConvertStageToMinLevel(int stage) {
        if(stage < 8) {
            return stage*5;
        } else {
            return (stage-8) * 10 + 40;
        }
    }

    /**
     * Gets the default user title for a stage.
     * @param stage The stage to get a user title for.
     * @return The title for the user at the specified stage.
     */

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

    /**
     * Gets the default user color for a stage.
     * @param stage The stage to get a user color for.
     * @return The color for the user at the specified stage.
     */

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
