package de.eldritch.spigot.discord_sync.user;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

// TODO: rewrite this mess
final class LegacyUserMap {
    private final Object lock = new Object();

    private final HashMap<Long, LegacyUser> turtleIndex    = new HashMap<>();
    private final HashMap<UUID, LegacyUser> uuidIndex      = new HashMap<>();
    private final HashMap<Long, LegacyUser> snowflakeIndex = new HashMap<>();

    /* ------------------------- */

    public LegacyUser get(long turtle) {
        synchronized (lock) {
            return turtleIndex.get(turtle);
        }
    }

    public LegacyUser getByUUID(@NotNull UUID uuid) {
        synchronized (lock) {
            return uuidIndex.get(uuid);
        }
    }

    public LegacyUser getBySnowflake(long snowflake) {
        synchronized (lock) {
            return snowflakeIndex.get(snowflake);
        }
    }

    public void remove(@NotNull LegacyUser user) {
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
     * Puts the {@link LegacyUser} into this UserMap. If a user with the same ID already exists it will be replaced.
     * @param user User to store in this map.
     * @return true if the User's ID is new to this map.
     */
    public boolean put(@NotNull LegacyUser user) {
        synchronized (lock) {
            if (user.minecraft() != null)
                uuidIndex.put(user.minecraft().getUniqueId(), user);
            if (user.discord() != null)
                snowflakeIndex.put(user.discord().getIdLong(), user);

            return turtleIndex.put(user.getID(), user) != null;
        }
    }

    /* ------------------------- */

    void registerIndex(UUID uuid, LegacyUser user) {
        uuidIndex.put(uuid, user);
    }

    void registerIndex(Long snowflake, LegacyUser user) {
        snowflakeIndex.put(snowflake, user);
    }

    /* ------------------------- */

    public Collection<LegacyUser> getUserView() {
        synchronized (lock) {
            return turtleIndex.values();
        }
    }
}
