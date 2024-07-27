package de.turtleboi.spigot.dsync.api;

import org.jetbrains.annotations.NotNull;

class Bootstrap {
    private static ObeliskAPI singleton;

    public synchronized static void register(@NotNull ObeliskAPI api) throws IllegalStateException {
        if (singleton != null)
            throw new IllegalStateException("An API implementation is already registered");
        singleton = api;
    }

    public static ObeliskAPI get() throws IllegalStateException {
        if (singleton == null)
            throw new IllegalStateException("No API implementation has been registered yet");
        return singleton;
    }
}
