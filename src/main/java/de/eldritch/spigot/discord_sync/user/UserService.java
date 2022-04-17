package de.eldritch.spigot.discord_sync.user;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.entities.EntityBuilder;
import de.eldritch.spigot.discord_sync.util.ConfigUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

// TODO: rewrite this mess
/**
 * Manages all cross-platform {@link User} objects.
 */
public class UserService {
    private final YamlConfiguration userConfiguration;
    private final YamlConfiguration blockConfiguration;

    private final UserMap userMap = new UserMap();

    public UserService() throws IOException, InvalidConfigurationException {
        // initialize config
        userConfiguration  = ConfigUtil.getConfig("users", null);
        blockConfiguration = ConfigUtil.getConfig("blocked", null);

        this.reloadUsers();
    }

    public void reloadUsers() {
        DiscordSync.singleton.getLogger().log(Level.INFO, "Reloading users...");

        Set<Long> turtles = userConfiguration
                .getKeys(false)
                .stream()
                .map(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }})
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Server server = DiscordSync.singleton.getServer();
        final Guild  guild  = DiscordSync.singleton.getDiscordService().getAccessor().getGuild();

        // schema: {new, mod, del, err}
        final int[] stats = {0, 0, 0, 0};

        // create & modify
        for (long turtle : turtles) {
            try {
                OfflinePlayer minecraft = null;
                Member        discord   = null;

                try {
                    final UUID uuid = UUID.fromString(userConfiguration.getString(turtle + ".uuid", "null"));

                    minecraft = server.getOfflinePlayer(uuid);
                } catch (IllegalArgumentException ignored) { }

                try {
                    final String snowflake = userConfiguration.getString(turtle + ".snowflake", "null");

                    discord = guild.getMemberById(snowflake);
                } catch (NumberFormatException ignored) { }

                String name = userConfiguration.getString(turtle + ".name", null);


                User user = new User(turtle, minecraft, discord, name);
                if (userMap.put(user))
                    stats[0]++;
                else
                    stats[1]++;
            } catch (Exception e) {
                DiscordSync.singleton.getLogger().log(Level.FINE, "Encountered an unexpected exception while parsing user " + turtle + ".", e);
                stats[3]++;
            }
        }

        // delete
        userMap.getUserView()
                .stream()
                .filter(user -> !turtles.contains(user.getID()))
                .forEach(user -> {
                    userMap.remove(user);
                    stats[2]++;
                });

        DiscordSync.singleton.getLogger().log(Level.INFO, String.format(
                        "Users reloaded. (%s created, %s modified, %s deleted, %s errors)",
                        stats[0], stats[1], stats[2], stats[3]
        ));
    }

    public void saveUsers() {
        // clear old config
        for (String key : userConfiguration.getKeys(true)) {
            userConfiguration.set(key, null);
        }

        for (User user : userMap.getUserView()) {
            OfflinePlayer player = user.minecraft();
            Member        member = user.discord();

            userConfiguration.set(user.getID() + ".uuid"     , player != null ? player.getUniqueId().toString() : null);
            userConfiguration.set(user.getID() + ".snowflake", member != null ? member.getId()                  : null);
        }
    }

    public void saveConfig() {
        try {
            ConfigUtil.saveConfig(userConfiguration, "users");
        } catch (IOException e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Encountered an unexpected exception while saving user config.", e);
        }

        try {
            ConfigUtil.saveConfig(blockConfiguration, "blocked");
        } catch (IOException e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Encountered an unexpected exception while saving block config.", e);
        }
    }

    /* ----- ----- ----- */

    public User getUser(long turtle) {
        User user = userMap.get(turtle);

        if (user == null) {
            user = newUser(null, null, null);
        }

        return user;
    }

    public User getUserBySnowflake(long snowflake) {
        User user = userMap.getBySnowflake(snowflake);

        if (user == null) {
            final Member discord = DiscordSync.singleton.getDiscordService().getAccessor().getGuild().getMemberById(snowflake);

            if (discord == null)
                throw new IllegalArgumentException("Could not find member matching snowflake " + snowflake);

            user = newUser(null, discord, discord.getEffectiveName());
        }

        return user;
    }

    public User getUserByUUID(UUID uuid) {
        User user = userMap.getByUUID(uuid);

        if (user == null) {
            final OfflinePlayer minecraft = DiscordSync.singleton.getServer().getOfflinePlayer(uuid);

            user = newUser(minecraft, null, minecraft.getName());
        }

        return user;
    }

    /* ----- ----- ----- */

    private @NotNull User newUser(@Nullable OfflinePlayer minecraft, @Nullable Member member, @Nullable String name) {
        User user = new User(EntityBuilder.TurtleBuilder.newID(), minecraft, member, name);
        userMap.put(user);

        DiscordSync.singleton.getLogger().log(Level.FINE, "Created user " + user.getID());

        saveUsers();
        saveConfig();

        return user;
    }

    /* ----- ----- ----- */

    public void register(@NotNull User user, Member member) {
        final User oldUser = getUserBySnowflake(member.getIdLong());

        if (oldUser.discord() != null)
            userMap.remove(oldUser);

        user.setDiscord(member);

        // update indexes
        userMap.registerIndex(member.getIdLong(), user);

        saveUsers();
        saveConfig();
    }

    public void register(@NotNull User user, OfflinePlayer player) {
        final User oldUser = getUserByUUID(player.getUniqueId());

        if (oldUser.minecraft() != null)
            userMap.remove(oldUser);

        // update indexes
        userMap.registerIndex(player.getUniqueId(), user);

        saveUsers();
        saveConfig();
    }

    /* ----- ----- ----- */

    public @NotNull YamlConfiguration getBlockedUsers() {
        return blockConfiguration;
    }
}
