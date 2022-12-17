package de.turtle_exception.discordsync.util.time;

import org.jetbrains.annotations.NotNull;

public final class Epoch {
    // 1970-01-01T00:00:00Z
    public static final Epoch UNIX     = new Epoch("Unix"    ,             0L);
    // 2015-01-01T00:00:00Z
    public static final Epoch DISCORD  = new Epoch("Discord" , 1420070400000L);
    // 2020-01-01T00:00:00Z
    public static final Epoch ELDRITCH = new Epoch("Eldritch", 1577836800000L);
    // 2022-01-01T00:00:00Z
    public static final Epoch TURTLE   = new Epoch("Turtle"  , 1640995200000L);

    /* - - - */

    private final String title;
    private final long   offset;

    public Epoch(@NotNull String title, long offset) {
        this.title  = title;
        this.offset = offset;
    }

    public String getTitle() {
        return title;
    }

    public long getOffset() {
        return offset;
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis() - getOffset();
    }
}