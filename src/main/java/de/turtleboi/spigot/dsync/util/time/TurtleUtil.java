package de.turtleboi.spigot.dsync.util.time;

import org.jetbrains.annotations.NotNull;

/**
 * A Turtle id is composed like this:
 * <pre> {@code
 * 111111111111111111111111111111111111111111 111111 11111111 11111111
 * 64                                         22     16       8       0
 * } </pre>
 * <ul>
 *     <li> 42 bits timestamp (Turtle Epoch)
 *     <li>  6 bits version information
 *     <li>  8 bits type information
 *     <li>  8 bits inc sequence
 * </ul>
 */
public class TurtleUtil {
    private static final int TIME_BITS = 41;
    private static final int  VER_BITS =  6;
    private static final int TYPE_BITS =  8;
    private static final int  INC_BITS =  8;

    private static long lastTime = getTime();

    // 0b000000 is UNKNOWN
    public static long VERSION = 0b000001;

    private static final int INC_MAX = 0b11111111;
    private static int increment = 0;

    private TurtleUtil() { }

    public static long newId(@NotNull TurtleType type) {
        return newId(type.getAsByte());
    }

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

    public static long getTime(long id) {
        return Epoch.TURTLE.getOffset() + (id >> TIME_BITS);
    }

    private static long getTime() {
        return Epoch.TURTLE.currentTimeMillis();
    }
}