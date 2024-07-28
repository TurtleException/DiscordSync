package de.turtleboi.spigot.dsync.api.entity.dao;

import de.turtleboi.spigot.dsync.api.entity.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public interface UserDAO {
    @NotNull User createUser(long id, @NotNull String name);

    @NotNull User provideUserByDiscord(long snowflake, @NotNull String name);
    @NotNull User provideUserByMinecraft(@NotNull UUID uuid, @NotNull String name);
    
    @NotNull List<User> listUsers();
    @NotNull Optional<User> getUser(long id);
    @NotNull Optional<User> getUserByDiscordId(long snowflake);
    @NotNull Optional<User> getUserByMinecraftId(@NotNull UUID uuid);
    @NotNull String getUserName(long id) throws NoSuchElementException;
    @NotNull List<Long> getUserDiscordAccounts(long id) throws NoSuchElementException;
    @NotNull List<UUID> getUserMinecraftAccounts(long id) throws NoSuchElementException;

    void setUserName(long id, @NotNull String name) throws NoSuchElementException;
    void addUserDiscordAccount(long id, long snowflake) throws NoSuchElementException;
    void removeUserDiscordAccount(long id, long snowflake) throws NoSuchElementException;
    void addUserMinecraftAccount(long id, @NotNull UUID uuid) throws NoSuchElementException;
    void removeUserMinecraftAccount(long id, @NotNull UUID uuid) throws NoSuchElementException;

    void deleteUser(long id);
}
