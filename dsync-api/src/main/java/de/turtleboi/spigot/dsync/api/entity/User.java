package de.turtleboi.spigot.dsync.api.entity;

import de.turtleboi.spigot.dsync.api.DiscordSyncAPI;
import de.turtleboi.spigot.dsync.api.entity.dao.UserDAO;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class User {
    private final @NotNull DiscordSyncAPI api;
    private final long id;

    public User(@NotNull DiscordSyncAPI api, long id) {
        this.api = api;
        this.id = id;
    }

    private @NotNull UserDAO getDAO() {
        return this.api.getUserDAO();
    }

    public long getId() {
        return this.id;
    }

    public @NotNull String getName() {
        return this.getDAO().getUserName(this.id);
    }

    public void setName(@NotNull String name) {
        this.getDAO().setUserName(this.id, name);
    }

    public @NotNull List<Long> getDiscordAccounts() {
        return Collections.unmodifiableList(this.getDAO().getUserDiscordAccounts(this.id));
    }

    public @NotNull List<UUID> getMinecraftAccounts() {
        return Collections.unmodifiableList(this.getDAO().getUserMinecraftAccounts(this.id));
    }

    public boolean hasDiscordAccount(long snowflake) {
        return this.getDiscordAccounts().contains(snowflake);
    }

    public boolean hasMinecraftAccount(@NotNull UUID uuid) {
        return this.getMinecraftAccounts().contains(uuid);
    }

    public void addDiscordAccount(long snowflake) {
        this.getDAO().addUserDiscordAccount(this.id, snowflake);
    }

    public void addMinecraftAccount(@NotNull UUID uuid) {
        this.getDAO().addUserMinecraftAccount(this.id, uuid);
    }

    public void removeDiscordAccount(long snowflake) {
        this.getDAO().removeUserDiscordAccount(this.id, snowflake);
    }

    public void removeMinecraftAccount(@NotNull UUID uuid) {
        this.getDAO().removeUserMinecraftAccount(this.id, uuid);
    }

    public void delete() {
        this.getDAO().deleteUser(this.id);
    }
}
