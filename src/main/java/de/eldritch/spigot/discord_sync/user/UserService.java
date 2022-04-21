package de.eldritch.spigot.discord_sync.user;

import com.google.common.collect.Sets;
import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.entities.EntityBuilder;
import de.eldritch.spigot.discord_sync.util.ConfigUtil;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.collections4.ListUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class UserService {
    private static final String CONFIG_NAME_USERS   = "users";
    private static final String CONFIG_NAME_BLOCKED = "blocked";

    /**
     * This is where all {@link User Users} are stored and indexed.
     */
    private final UserMap userMap = new UserMap();

    /**
     * The <code>users.yml</code> configuration.
     */
    private final YamlConfiguration userConfiguration;
    /**
     * The <code>blocked.yml</code> configuration.
     */
    private final YamlConfiguration blockConfiguration;


    public UserService() throws IOException, InvalidConfigurationException {
        // initialize config
        userConfiguration  = ConfigUtil.getConfig(CONFIG_NAME_USERS,   null);
        blockConfiguration = ConfigUtil.getConfig(CONFIG_NAME_BLOCKED, null);

        this.reloadUsers();
    }

    /* ----- ----- ----- */

    public void reloadUsers() {
        final Set<User> usersOld = userMap.getView();
        final Set<User> usersNew = ConfigParser.ofUserConfig(userConfiguration, this);

        // schema: {copied, new, del, mod}
        final int[] stats = {0, 0, 0, 0};

        final Set<Long> turtlesOld = usersOld.stream().map(User::getID).collect(Collectors.toSet());
        final Set<Long> turtlesNew = usersNew.stream().map(User::getID).collect(Collectors.toSet());

        stats[1] = Sets.difference(turtlesNew, turtlesOld).size();
        stats[2] = Sets.difference(turtlesOld, turtlesNew).size();

        for (User user1 : usersNew) {
            for (User user2 : usersOld) {
                if (user1.getID() == user2.getID())
                    continue;

                if (UserUtil.isEqual(user1, user2)) {
                    // copied
                    stats[0]++;
                } else {
                    // modified
                    stats[3]++;
                }
            }
        }

        DiscordSync.singleton.getLogger().log(Level.INFO, String.format(
                "Users reloaded. (%s copied, %s created, %s deleted, %s modified)",
                stats[0], stats[1], stats[2], stats[3]
        ));
    }

    public void saveUsers() {
        // clear old config
        for (String key : userConfiguration.getKeys(true)) {
            userConfiguration.set(key, null);
        }

        for (User user : userMap.getView()) {
            final OfflinePlayer player = user.minecraftOffline();
            final Member        member = user.discord();

            userConfiguration.set(user.getID() + ".uuid"     , player != null ? player.getUniqueId().toString() : null);
            userConfiguration.set(user.getID() + ".snowflake", member != null ? member.getId()                  : null);
        }

        /* ----- ----- ----- */

        for (String key : blockConfiguration.getKeys(true)) {
            blockConfiguration.set(key, null);
        }


    }

    public void saveConfig() {
        try {
            ConfigUtil.saveConfig(userConfiguration, CONFIG_NAME_USERS);
        } catch (IOException e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Encountered an unexpected exception while saving user config.", e);
        }

        /* ----- ----- ----- */

        try {
            ConfigUtil.saveConfig(blockConfiguration, CONFIG_NAME_BLOCKED);
        } catch (IOException e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Encountered an unexpected exception while saving block config.", e);
        }
    }

    /* ----- ----- ----- */

    public @NotNull User ofTurtle(long turtle) {
        User user = userMap.getByTurtle(turtle);

        return user != null
                ? user
                : new UserBuilder()
                    .setTurtle(EntityBuilder.TurtleBuilder.newID())
                    .setUserService(this)
                    .build();
    }

    public @NotNull User ofSnowflake(long snowflake) {
        User user = userMap.getBySnowflake(snowflake);

        return user != null
                ? user
                : new UserBuilder()
                    .setTurtle(EntityBuilder.TurtleBuilder.newID())
                    .setMember(UserUtil.getMember(snowflake, null))
                    .setUserService(this)
                    .build();
    }

    public @NotNull User ofUUID(@NotNull UUID uuid) {
        User user = userMap.getByUUID(uuid);

        return user != null
                ? user
                : new UserBuilder()
                    .setTurtle(EntityBuilder.TurtleBuilder.newID())
                    .setPlayer(UserUtil.getPlayer(uuid, null))
                    .setUserService(this)
                    .build();
    }

    /* ----- ----- ----- */

    public @Nullable User getByTurtle(long turtle) {
        return userMap.getByTurtle(turtle);
    }

    public @Nullable User getBySnowflake(long snowflake) {
        return userMap.getBySnowflake(snowflake);
    }

    public @Nullable User getByUUID(@NotNull UUID uuid) {
        return userMap.getByUUID(uuid);
    }

    /* ----- ----- ----- */

    private final Object LOCK = new Object();

    /**
     * Handles a blocking interaction.
     * @param key Key to the blocking user.
     * @param value The blocked user or "*" for a global block.
     */
    public void addBlock(@NotNull String key, @NotNull String value) {
        synchronized (LOCK) {
            // update blocking list
            final List<String> blockedOld = blockConfiguration.getStringList(key);
            final List<String> blockedNew = ListUtils.union(blockedOld, List.of(value));

            // update config
            blockConfiguration.set(key, blockedNew);
        }
    }

    /**
     * Removes a blocking interaction.
     * @param key Key to the blocking user.
     * @param value The blocked user or "*" for a global block.
     */
    public void removeBlock(@NotNull String key, @NotNull String value) {
        synchronized (LOCK) {
            // update blocking list
            final List<String> blockedOld = blockConfiguration.getStringList(key);
            final List<String> blockedNew = ListUtils.subtract(blockedOld, List.of(value));

            // update config
            blockConfiguration.set(key, blockedNew);
        }
    }

    /**
     * Indicates whether a user is blocked by another server.
     * @param key Key to the blocking user.
     * @param value The blocked user or "*" for a global block.
     * @param checkGlobal Whether to also check for a global block.
     * @return <code>true</code> if the provided user is blocked or a global blocking interaction is in place and
     *         checkGlobal is also <code>true</code>.
     */
    public boolean isBlocked(@NotNull String key, @NotNull String value, boolean checkGlobal) {
        final List<String> blocked = blockConfiguration.getStringList(key);

        return blocked.contains(value) || (blocked.contains("*") && !checkGlobal);
    }

    /* ----- ----- ----- */

    @NotNull UserMap getMap() {
        return userMap;
    }

    void register(User user) {
        userMap.put(user);
    }
}
