package de.turtle_exception.discordsync.message.source;

import org.jetbrains.annotations.NotNull;

public class ServerSource extends Source {
    private static final ServerSource INSTANCE = new ServerSource();

    private ServerSource() { }

    public static @NotNull ServerSource get() {
        return INSTANCE;
    }
}
