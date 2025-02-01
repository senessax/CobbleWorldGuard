package dev.zanckor.cobbleguard.util

import java.util.HashMap


object Timer {
    private val TIMERS = HashMap<String, Long>()

    /**
     * Start a timer with a identifier name and time in seconds to reach.
     * The timer reaches when has been running for the timeInSeconds.
     *
     * @param name          The identifier name of the timer.
     * @param timeInSeconds The time in seconds to reach.
     */
    fun start(name: String, timeInSeconds: Double) {
        TIMERS[name] = (System.currentTimeMillis() + (timeInSeconds * 1000)).toLong()
    }

    /**
     * Check if the timer has been initialized.
     *
     * @param name The identifier name of the timer.
     * @return True if the timer has been initialized.
     */
    fun hasTimer(name: String): Boolean {
        return TIMERS.containsKey(name)
    }

    /**
     * Check if the timer has reached the timeInSeconds.
     *
     * @param name          The identifier name of the timer.
     * @param removeOnReach Remove the timer when it reaches the timeInSeconds.
     * @return True if the timer has reached the timeInSeconds.
     */
    fun hasReached(name: String, removeOnReach: Boolean): Boolean {
        if (TIMERS.containsKey(name)) {
            val timer = TIMERS[name]!!

            if (System.currentTimeMillis() >= timer) {
                if (removeOnReach) TIMERS.remove(name)
                return true
            }
        } else {
            return true
        }

        return false
    }

    /**
     * Check if the timer has reached the timeInSeconds. The timer will not be removed, but will restart with the specified timeInSeconds.
     *
     * @param name          The identifier name of the timer.
     * @param timeInSeconds The time in seconds to reach.
     * @return True if the timer has reached the timeInSeconds.
     */
    fun hasReached(name: String, timeInSeconds: Double): Boolean {
        if (TIMERS.containsKey(name)) {
            val timer = TIMERS[name]!!

            if (System.currentTimeMillis() >= timer) {
                start(name, timeInSeconds)
                return true
            }
        } else {
            start(name, timeInSeconds)
            return true
        }

        return false
    }

    /**
     * Check if the timer is running.
     *
     * @param name The identifier name of the timer.
     * @return True if the timer is running.
     */
    fun isRunning(name: String): Boolean {
        return TIMERS.containsKey(name) && hasReached(name, false)
    }

    /**
     * Add time to the timer.
     *
     * @param name          The identifier name of the timer.
     * @param timeInSeconds The time in seconds to add.
     */
    fun addTime(name: String, timeInSeconds: Long) {
        if (TIMERS.containsKey(name)) {
            TIMERS[name] = TIMERS[name]!! + (timeInSeconds * 1000)
        }
    }

    /**
     * Get the time in seconds to finish the timer.
     *
     * @param name The identifier name of the timer.
     * @return The time in seconds to finish the timer.
     */
    fun timeToFinish(name: String): Int {
        if (TIMERS.containsKey(name)) {
            return ((TIMERS[name]!! - System.currentTimeMillis()) / 1000).toInt()
        }

        return -1
    }
}
