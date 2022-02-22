package de.eldritch.spigot.discord_sync.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

final class UserMap {
    private final HashMap<Long, User> users = new HashMap<>();
    private long index = 0;

    private final HashMap<UUID, Long> uuidIndex      = new HashMap<>();
    private final HashMap<Long, Long> snowflakeIndex = new HashMap<>();

    /* ------------------------- */

    private @Nullable User getByIndex(long index) {
        return users.get(index);
    }

    /* ------------------------- */

    public User get(@NotNull UUID uuid) {
        return this.getByIndex(uuidIndex.get(uuid));
    }

    public User get(long snowflake) {
        return this.getByIndex(snowflakeIndex.get(snowflake));
    }

    public void remove(@NotNull User user) {
        if (!users.containsValue(user)) return;

        snowflakeIndex.remove(user.snowflake());
        users.remove(uuidIndex.remove(user.uuid()), user);
    }

    /* ------------------------- */

    /**
     * Puts the {@link User} into this UserMap. If a user with the same {@link UUID} already exists it will be replaced.
     * @param user User to store in this map.
     * @return true if the User's UUID is new to this map.
     */
    public boolean put(@NotNull User user) {
        long old   = uuidIndex.getOrDefault(user.uuid(), -1L);
        long index = old >= 0 ? old : this.index++;

        users.put(index, user);
        if (old != index) {
            uuidIndex.put(user.uuid(), index);
            if (user.isDiscordConnected())
                snowflakeIndex.put(user.snowflake(), index);
        }

        return old != index;
    }

    /* ------------------------- */

    public Collection<User> getUserView() {
        return users.values();
    }
}
