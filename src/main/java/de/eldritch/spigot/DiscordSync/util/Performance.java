package de.eldritch.spigot.DiscordSync.util;

/**
 * https://bukkit.org/threads/get-server-tps.143410/
 * @author LazyLemons (modified)
 */
public class Performance implements java.lang.Runnable {
    public static int TICK_COUNT = 0;
    public static long[] TICKS = new long[600];

    @Override
    public void run() {
        TICKS[(TICK_COUNT % TICKS.length)] = System.currentTimeMillis();
        TICK_COUNT++;
    }

    public static double getTPS(int ticks) {
        if (TICK_COUNT < ticks)
            return 20.0D;

        int target = (TICK_COUNT - 1 - ticks) % TICKS.length;
        long elapsed = System.currentTimeMillis() - TICKS[target];

        return ticks / (elapsed / 1000.0D);
    }

    public static int getLagPercent() {
        int lag = (int) Math.round((1.0D - Performance.getTPS(100) / 20.0D) * 100.0D);
        return Math.max(lag, 0);
    }
}