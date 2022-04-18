package de.eldritch.spigot.discord_sync.user;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.util.MiscUtil;
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.Server;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Utility class to deserialize user- and blocked-configuration.
 */
public class ConfigParser {
    /**
     * Parses a {@link MemoryConfiguration} to a {@link Set} of Users.
     * @param config The configuration that contains serialized users.
     * @param uService {@link UserService} that the new objects should be bound to.
     * @return Set of deserialized user-objects
     */
    public static @NotNull Set<User> ofUserConfig(@NotNull MemoryConfiguration config, @NotNull UserService uService) {
        final Set<Long> turtles = config.getKeys(false)
                .stream()
                .map(str -> {
                    try {
                        return Long.parseLong(str);
                    } catch (NumberFormatException | NullPointerException e) {
                        MiscUtil.logUnexpectedException(Level.FINE, "when mapping \"" + str + "\" to Turtle ID.", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Server server = DiscordSync.singleton.getServer();
        final Guild  guild  = DiscordSync.singleton.getDiscordService().getAccessor().getGuild();

        HashSet<User> users = new HashSet<>();

        for (long turtle : turtles) {
            try {
                users.add(new UserBuilder()
                        .setUserService(uService)
                        .setPlayer(UserUtil.getPlayer(config.getString(turtle + ".uuid"), server))
                        .setMember(UserUtil.getMember(config.getString(turtle + ".snowflake"), guild))
                        .setName(config.getString(turtle + ".name"))
                        .build()
                );
            } catch (IllegalArgumentException e) {
                MiscUtil.logUnexpectedException(Level.WARNING, "when parsing user with turtle ID " + turtle + ".", e);
            }
        }

        return Set.copyOf(users);
    }
}
