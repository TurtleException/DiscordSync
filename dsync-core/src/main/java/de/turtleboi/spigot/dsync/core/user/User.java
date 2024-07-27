package de.turtleboi.spigot.dsync.core.user;

import de.turtleboi.spigot.dsync.util.CloseableLock;
import de.turtleboi.spigot.dsync.util.ReadWriteCloseableLock;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class User {
    private final long id;
    private final @NotNull String name;

    private final List<Long>   discordAccounts;
    private final List<UUID> minecraftAccounts;

    private final ReadWriteCloseableLock lock;

    User(long id, @NotNull String name) {
        this(id, name, List.of(), List.of());
    }

    User(long id, @NotNull String name, @NotNull List<Long> discordAccounts, @NotNull List<UUID> minecraftAccounts) {
        this.id   = id;
        this.name = name;

        this.discordAccounts   = new ArrayList<>(discordAccounts);
        this.minecraftAccounts = new ArrayList<>(minecraftAccounts);

        this.lock = new ReadWriteCloseableLock();
    }

    void addDiscord(long snowflake) {
        try (CloseableLock ignored = this.lock.write()) {
            this.discordAccounts.add(snowflake);
        }
    }

    void addMinecraft(@NotNull UUID uuid) {
        try (CloseableLock ignored = this.lock.write()) {
            this.minecraftAccounts.add(uuid);
        }
    }

    void removeDiscord(long snowflake) {
        try (CloseableLock ignored = this.lock.write()) {
            this.discordAccounts.remove(snowflake);
        }
    }

    void removeMinecraft(@NotNull UUID uuid) {
        try (CloseableLock ignored = this.lock.write()) {
            this.minecraftAccounts.remove(uuid);
        }
    }

    public @NotNull List<Long> getDiscordAccounts() {
        try (CloseableLock ignored = this.lock.read()) {
            return List.copyOf(this.discordAccounts);
        }
    }

    public @NotNull List<UUID> getMinecraftAccounts() {
        try (CloseableLock ignored = this.lock.read()) {
            return List.copyOf(this.minecraftAccounts);
        }
    }

    public boolean hasDiscordAccount(long snowflake) {
        try (CloseableLock ignored = this.lock.read()) {
            return this.discordAccounts.contains(snowflake);
        }
    }

    public boolean hasMinecraftAccount(@NotNull UUID uuid) {
        try (CloseableLock ignored = this.lock.read()) {
            return this.minecraftAccounts.contains(uuid);
        }
    }
}
