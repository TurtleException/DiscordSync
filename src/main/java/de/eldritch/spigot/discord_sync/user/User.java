package de.eldritch.spigot.discord_sync.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record User(@NotNull UUID uuid, long snowflake, @Nullable String name) {
    public boolean isDiscordConnected() {
        return snowflake > 0;
    }
}
