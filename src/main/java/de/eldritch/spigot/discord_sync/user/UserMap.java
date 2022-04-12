package de.eldritch.spigot.discord_sync.user;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

final class UserMap {
    private final Object lock = new Object();

    private final HashMap<Long, User> turtleIndex    = new HashMap<>();
    private final HashMap<UUID, User> uuidIndex      = new HashMap<>();
    private final HashMap<Long, User> snowflakeIndex = new HashMap<>();

    /* ------------------------- */

    public User get(long turtle) {
        synchronized (lock) {
            return turtleIndex.get(turtle);
        }
    }

    public User getByUUID(@NotNull UUID uuid) {
        synchronized (lock) {
            return uuidIndex.get(uuid);
        }
    }

    public User getBySnowflake(long snowflake) {
        synchronized (lock) {
            return snowflakeIndex.get(snowflake);
        }
    }

    public void remove(@NotNull User user) {
        synchronized (lock) {
            turtleIndex.remove(user.getID());

            if (user.minecraft() != null)
                uuidIndex.remove(user.minecraft().getUniqueId());
            if (user.discord() != null)
                snowflakeIndex.remove(user.discord().getIdLong());
        }
    }

    /* ------------------------- */

    /**
     * Puts the {@link User} into this UserMap. If a user with the same {@link UUID} already exists it will be replaced.
     * @param user User to store in this map.
     * @return true if the User's UUID is new to this map.
     */
    public boolean put(@NotNull User user) {
        synchronized (lock) {
            if (user.minecraft() != null)
                uuidIndex.put(user.minecraft().getUniqueId(), user);
            if (user.discord() != null)
                snowflakeIndex.put(user.discord().getIdLong(), user);

            return turtleIndex.put(user.getID(), user) != null;
        }
    }

    /* ------------------------- */

    public Collection<User> getUserView() {
        synchronized (lock) {
            return turtleIndex.values();
        }
    }
}
