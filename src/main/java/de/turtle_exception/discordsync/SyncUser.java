package de.turtle_exception.discordsync;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SyncUser implements Entity {
    private final long id;

    private final ArrayList<UUID> minecraftIds;
    private final ArrayList<Long>   discordIds;

    SyncUser(long id, List<UUID> minecraftIds, List<Long> discordIds) {
        this.id = id;
        this.minecraftIds = new ArrayList<>(minecraftIds);
        this.discordIds   = new ArrayList<>(discordIds);
    }

    @Override
    public long id() {
        return this.id;
    }

    public @NotNull List<UUID> getMinecraftIds() {
        return List.copyOf(minecraftIds);
    }

    public @NotNull List<Long> getDiscordIds() {
        return List.copyOf(discordIds);
    }
}
