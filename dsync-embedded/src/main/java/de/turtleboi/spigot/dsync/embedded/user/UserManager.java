package de.turtleboi.spigot.dsync.embedded.user;

import de.turtleboi.spigot.dsync.api.entity.User;
import de.turtleboi.spigot.dsync.api.entity.dao.UserDAO;
import de.turtleboi.spigot.dsync.embedded.DiscordSyncEmbedded;
import de.turtleboi.spigot.dsync.util.CloseableLock;
import de.turtleboi.spigot.dsync.util.IdUtil;
import de.turtleboi.spigot.dsync.util.ReadWriteCloseableLock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class UserManager implements UserDAO {
    private final DiscordSyncEmbedded plugin;
    private final YamlConfiguration config;

    private final ReadWriteCloseableLock lock = new ReadWriteCloseableLock();

    public UserManager(@NotNull DiscordSyncEmbedded plugin, @NotNull YamlConfiguration userConfig) {
        this.plugin = plugin;
        this.config = userConfig;
    }

    @Override
    public @NotNull User createUser(long id, @NotNull String name) {
        try (CloseableLock ignored = this.lock.write()) {
            ConfigurationSection section = config.createSection(String.valueOf(id));

            section.set("name", name);
        }

        return new User(this.plugin, id);
    }

    public @NotNull User createUser(@NotNull String name) {
        return this.createUser(IdUtil.newId(((byte) "dsync-user".hashCode())), name);
    }

    @Override
    public @NotNull User provideUserByDiscord(long snowflake, @NotNull String name) {
        try (CloseableLock ignored = this.lock.write()) {
            return this.getUserByDiscordId(snowflake).orElseGet(() -> {
                // no user has claimed the discord id yet, create a new one
                User user = this.createUser(name);
                user.addDiscordAccount(snowflake);
                return user;
            });
        }
    }

    @Override
    public @NotNull User provideUserByMinecraft(@NotNull UUID uuid, @NotNull String name) {
        try (CloseableLock ignored = this.lock.write()) {
            return this.getUserByMinecraftId(uuid).orElseGet(() -> {
                // no user has claimed the discord id yet, create a new one
                User user = this.createUser(name);
                user.addMinecraftAccount(uuid);

                return user;
            });
        }
    }

    @Override
    public @NotNull List<User> listUsers() {
        List<User> users = new ArrayList<>();

        try (CloseableLock ignored = this.lock.read()) {
            for (String key : this.config.getKeys(false)) {
                try {
                    long id = Long.parseLong(key);

                    users.add(new User(this.plugin, id));
                } catch (NumberFormatException e) {
                    this.plugin.getLogger().log(Level.WARNING, "Invalid key in user config: " + key);
                }
            }
        }

        return users;
    }

    @Override
    public @NotNull Optional<User> getUser(long id) {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.read()) {
            if (this.config.contains(idStr))
                return Optional.of(new User(this.plugin, id));
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Optional<User> getUserByDiscordId(long snowflake) {
        try (CloseableLock ignored = this.lock.read()) {
            for (String key : this.config.getKeys(false)) {
                List<Long> snowflakes = this.config.getLongList(key + ".discord");

                if (snowflakes.contains(snowflake)) {
                    try {
                        long id = Long.parseLong(key);

                        return Optional.of(new User(this.plugin, id));
                    } catch (NumberFormatException e) {
                        this.plugin.getLogger().log(Level.WARNING, "Invalid key in user config: " + key);

                        // ignore invalid keys and continue iterating
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Optional<User> getUserByMinecraftId(@NotNull UUID uuid) {
        String uuidStr = uuid.toString();

        try (CloseableLock ignored = this.lock.read()) {
            for (String key : this.config.getKeys(false)) {
                List<String> uuids = this.config.getStringList(key + ".minecraft");

                if (uuids.contains(uuidStr)) {
                    try {
                        long id = Long.parseLong(key);

                        return Optional.of(new User(this.plugin, id));
                    } catch (NumberFormatException e) {
                        this.plugin.getLogger().log(Level.WARNING, "Invalid key in user config: " + key);

                        // ignore invalid keys and continue iterating
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull String getUserName(long id) throws NoSuchElementException {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.read()) {
            ConfigurationSection section = this.config.getConfigurationSection(idStr);

            if (section != null) {
                String name = section.getString("name");

                if (name != null)
                    return name;
            }
        }

        throw new NoSuchElementException("Unknown user: " + id);
    }

    @Override
    public @NotNull List<Long> getUserDiscordAccounts(long id) throws NoSuchElementException {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.read()) {
            ConfigurationSection section = this.config.getConfigurationSection(idStr);

            if (section != null)
                return section.getLongList("discord");
        }

        throw new NoSuchElementException("Unknown user: " + id);
    }

    @Override
    public @NotNull List<UUID> getUserMinecraftAccounts(long id) throws NoSuchElementException {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.read()) {
            ConfigurationSection section = this.config.getConfigurationSection(idStr);

            if (section != null) {
                List<String> uuidStrings = section.getStringList("minecraft");
                List<UUID>   uuids       = new ArrayList<>(uuidStrings.size());

                for (String uuidStr : uuidStrings) {
                    try {
                        uuids.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException e) {
                        this.plugin.getLogger().log(Level.WARNING, "Invalid UUID in " + idStr + ".minecraft: " + uuidStr);

                        // skip invalid UUIDs and continue iterating
                    }
                }

                return uuids;
            }
        }

        throw new NoSuchElementException("Unknown user: " + id);
    }

    @Override
    public void setUserName(long id, @NotNull String name) throws NoSuchElementException {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.write()) {
            ConfigurationSection section = this.config.getConfigurationSection(idStr);

            if (section == null)
                throw new NoSuchElementException("Unknown user: " + id);

            section.set("name", name);
        }
    }

    @Override
    public void addUserDiscordAccount(long id, long snowflake) throws NoSuchElementException {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.write()) {
            ConfigurationSection section = this.config.getConfigurationSection(idStr);

            if (section == null)
                throw new NoSuchElementException("Unknown user: " + id);

            List<Long> snowflakes = section.getLongList("discord");
            snowflakes.add(snowflake);

            section.set("discord", snowflakes);
        }
    }

    @Override
    public void removeUserDiscordAccount(long id, long snowflake) throws NoSuchElementException {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.write()) {
            ConfigurationSection section = this.config.getConfigurationSection(idStr);

            if (section == null)
                throw new NoSuchElementException("Unknown user: " + id);

            List<Long> snowflakes = section.getLongList("discord");
            snowflakes.remove(snowflake);

            section.set("discord", snowflakes);
        }
    }

    @Override
    public void addUserMinecraftAccount(long id, @NotNull UUID uuid) throws NoSuchElementException {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.write()) {
            ConfigurationSection section = this.config.getConfigurationSection(idStr);

            if (section == null)
                throw new NoSuchElementException("Unknown user: " + id);

            List<String> uuids = section.getStringList("minecraft");
            uuids.add(uuid.toString());

            section.set("minecraft", uuids);
        }
    }

    @Override
    public void removeUserMinecraftAccount(long id, @NotNull UUID uuid) throws NoSuchElementException {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.write()) {
            ConfigurationSection section = this.config.getConfigurationSection(idStr);

            if (section == null)
                throw new NoSuchElementException("Unknown user: " + id);

            List<String> uuids = section.getStringList("minecraft");
            uuids.remove(uuid.toString());

            section.set("minecraft", uuids);
        }
    }

    @Override
    public void deleteUser(long id) {
        String idStr = String.valueOf(id);

        try (CloseableLock ignored = this.lock.write()) {
            this.config.set(idStr, null);
        }
    }
}
