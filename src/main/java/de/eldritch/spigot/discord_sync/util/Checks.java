package de.eldritch.spigot.discord_sync.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Checks {
    public static <T> T orElseIfNull(@Nullable T obj, @NotNull T alt) {
        return obj != null ? obj : alt;
    }
}
