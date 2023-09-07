package de.turtleboi.spigot.dsync.message.source;

import org.jetbrains.annotations.NotNull;

public class ServerSource extends Source {
    private static final ServerSource INSTANCE = new ServerSource();

    private ServerSource() { }

    public static @NotNull ServerSource get() {
        return INSTANCE;
    }
}
