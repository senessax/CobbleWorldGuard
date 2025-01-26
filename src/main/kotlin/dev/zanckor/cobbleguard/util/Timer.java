package dev.zanckor.cobbleguard.util;

import java.util.HashMap;

public class Timer {
    private static final HashMap<String, Long> TIMERS = new HashMap<>();

    /**
     * Start a timer with a identifier name and time in seconds to reach.
     * The timer reaches when has been running for the timeInSeconds.
     *
     * @param name          The identifier name of the timer.
     * @param timeInSeconds The time in seconds to reach.
     */
    public static void start(String name, double timeInSeconds) {
        TIMERS.put(name, (long) (System.currentTimeMillis() + (timeInSeconds * 1000)));
    }

    /**
     * Check if the timer has been initialized.
     *
     * @param name The identifier name of the timer.
     * @return True if the timer has been initialized.
     */
    public static boolean hasTimer(String name) {
        return TIMERS.containsKey(name);
    }

    /**
     * Check if the timer has reached the timeInSeconds.
     *
     * @param name          The identifier name of the timer.
     * @param removeOnReach Remove the timer when it reaches the timeInSeconds.
     * @return True if the timer has reached the timeInSeconds.
     */
    public static boolean hasReached(String name, boolean removeOnReach) {
        if (TIMERS.containsKey(name)) {
            long timer = TIMERS.get(name);

            if (System.currentTimeMillis() >= timer) {
                if (removeOnReach) TIMERS.remove(name);
                return true;
            }
        } else {
            return true;
        }

        return false;
    }

    /**
     * Check if the timer has reached the timeInSeconds. The timer will not be removed, but will restart with the specified timeInSeconds.
     *
     * @param name          The identifier name of the timer.
     * @param timeInSeconds The time in seconds to reach.
     * @return True if the timer has reached the timeInSeconds.
     */
    public static boolean hasReached(String name, int timeInSeconds) {
        if (TIMERS.containsKey(name)) {
            long timer = TIMERS.get(name);

            if (System.currentTimeMillis() >= timer) {
                start(name, timeInSeconds);
                return true;
            }
        } else {
            start(name, timeInSeconds);
            return true;
        }

        return false;
    }

    /**
     * Check if the timer is running.
     *
     * @param name The identifier name of the timer.
     * @return True if the timer is running.
     */
    public static boolean isRunning(String name) {
        return TIMERS.containsKey(name) && hasReached(name, false);
    }

    /**
     * Add time to the timer.
     *
     * @param name          The identifier name of the timer.
     * @param timeInSeconds The time in seconds to add.
     */
    public static void addTime(String name, long timeInSeconds) {
        if (TIMERS.containsKey(name)) {
            TIMERS.put(name, TIMERS.get(name) + (timeInSeconds * 1000));
        }
    }

    /**
     * Get the time in seconds to finish the timer.
     *
     * @param name The identifier name of the timer.
     * @return The time in seconds to finish the timer.
     */
    public static int timeToFinish(String name) {
        if (TIMERS.containsKey(name)) {
            return (int) ((TIMERS.get(name) - System.currentTimeMillis()) / 1000);
        }

        return -1;
    }
}
