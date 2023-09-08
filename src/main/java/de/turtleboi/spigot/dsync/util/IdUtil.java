package de.turtleboi.spigot.dsync.util;

/**
 * An id is composed like this:
 * <pre> {@code
 * 111111111111111111111111111111111111111111 111111 11111111 11111111
 * 64                                         22     16       8       0
 * } </pre>
 * <ul>
 *     <li> 42 bits timestamp (DSync Epoch)
 *     <li>  6 bits version information
 *     <li>  8 bits type information
 *     <li>  8 bits inc sequence
 * </ul>
 */
public class IdUtil {
    private static final int TIME_BITS = 41;
    private static final int  VER_BITS =  6;
    private static final int TYPE_BITS =  8;
    private static final int  INC_BITS =  8;

    /** 2023-07-22 04:26:40 */
    public static final long EPOCH = 1690000000000L;

    private static long lastTime = getTime();

    // 0b000000 is UNKNOWN
    public static long VERSION = 0b000001;

    private static final int INC_MAX = 0b11111111;
    private static int increment = 0;

    private IdUtil() { }

    @SuppressWarnings("StatementWithEmptyBody")
    public static synchronized long newId(byte type) {
        long time = getTime();

        if (time < lastTime)
            throw new IllegalStateException("Illegal system time!");

        if (time == lastTime) {
            increment = (increment + 1) & INC_MAX;

            if (increment == 0)
                while ((time = getTime()) == lastTime) { }
        } else {
            increment = 0;
        }

        lastTime = time;

        return time << (VER_BITS + TYPE_BITS + INC_BITS)
                | (VERSION << (TYPE_BITS + INC_BITS))
                | (type << (INC_BITS))
                | (increment);
    }

    /** Returns the unix timestamp of this id. */
    public static long getTime(long id) {
        return EPOCH + (id >> TIME_BITS);
    }

    /** Provides the current time of millis in the DSync epoch. */
    private static long getTime() {
        return System.currentTimeMillis() - EPOCH;
    }
}