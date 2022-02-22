package de.eldritch.spigot.discord_sync.user;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.util.ConfigUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages all cross-platform {@link User} objects.
 */
public class UserService {
    private YamlConfiguration userConfiguration;

    private final UserMap userMap = new UserMap();

    public UserService() throws IOException, InvalidConfigurationException {
        // initialize config
        userConfiguration = ConfigUtil.getConfig("users", null);

        this.reloadUsers();
    }

    public void reloadUsers() {
        DiscordSync.singleton.getLogger().log(Level.INFO, "Reloading users...");

        Set<String> uuids = userConfiguration.getKeys(false);

        // schema: {new, mod, del}
        final int[] stats = {0, 0, 0};

        // create & modify
        for (String uuidStr : uuids) {
            UUID   uuid      = UUID.fromString(uuidStr);
            long   snowflake = userConfiguration.getLong(uuidStr + ".snowflake", -1);
            String name      = userConfiguration.getString(uuidStr + "name", null);

            User user = new User(uuid, snowflake, name);
            if (userMap.put(user))
                stats[0]++;
            else
                stats[1]++;
        }

        // delete
        userMap.getUserView()
                .stream()
                .map(user -> user.uuid().toString())
                .filter(uuids::contains)
                .forEach(uuid -> {
                    userMap.remove(UUID.fromString(uuid));
                    stats[2]++;
                });

        DiscordSync.singleton.getLogger().log(Level.INFO, String.format(
                        "Users reloaded. (%s created, %s modified, %s deleted)",
                        stats[0], stats[1], stats[2]
        ));
    }
}
