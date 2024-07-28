package de.turtleboi.spigot.dsync.api;

import org.jetbrains.annotations.NotNull;

class Bootstrap {
    private static DiscordSyncAPI singleton;

    public synchronized static void register(@NotNull DiscordSyncAPI api) throws IllegalStateException {
        if (singleton != null)
            throw new IllegalStateException("An API implementation is already registered");
        singleton = api;
    }

    public static DiscordSyncAPI get() throws IllegalStateException {
        if (singleton == null)
            throw new IllegalStateException("No API implementation has been registered yet");
        return singleton;
    }
}
