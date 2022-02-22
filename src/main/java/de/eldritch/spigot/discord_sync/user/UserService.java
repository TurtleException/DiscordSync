package de.eldritch.spigot.discord_sync.user;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.util.ConfigUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.Collection;
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

        // stats
        int sAdded    = 0;
        int sModified = 0;
        int sDeleted  = 0;

        Collection<User> userView = userMap.getUserView();

        for (String uuidStr : userConfiguration.getKeys(false)) {
            UUID   uuid      = UUID.fromString(uuidStr);
            long   snowflake = userConfiguration.getLong(uuidStr + ".snowflake", -1);
            String name      = userConfiguration.getString(uuidStr + "name", null);

            User user = new User(uuid, snowflake, name);
            if (userMap.put(user))
                sAdded++;
            else
                sModified++;
        }

        // TODO: handle stats
        // (do we really need those?)
    }
}
