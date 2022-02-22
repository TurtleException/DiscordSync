package de.eldritch.spigot.discord_sync.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class UserMap {
    private final HashMap<UUID, User> uuidIndex      = new HashMap<>();
    private final HashMap<Long, User> snowflakeIndex = new HashMap<>();

    /* ------------------------- */

    public User get(@NotNull UUID uuid) {
        return uuidIndex.get(uuid);
    }

    public User get(long snowflake) {
        return snowflakeIndex.get(snowflake);
    }

    public void remove(@NotNull User user) {
        uuidIndex.remove(user.uuid());
        snowflakeIndex.remove(user.snowflake());
    }

    public void remove(@NotNull UUID uuid) {
        User user = uuidIndex.remove(uuid);
        if (user != null)
            snowflakeIndex.remove(user.snowflake());
    }

    public void remove(@NotNull String uuid) {
        remove(UUID.fromString(uuid));
    }

    /* ------------------------- */

    /**
     * Puts the {@link User} into this UserMap. If a user with the same {@link UUID} already exists it will be replaced.
     * @param user User to store in this map.
     * @return true if the User's UUID is new to this map.
     */
    public boolean put(@NotNull User user) {
        if (user.isDiscordConnected())
            snowflakeIndex.put(user.snowflake(), user);
        return uuidIndex.put(user.uuid(), user) != null;
    }

    /* ------------------------- */

    public Collection<User> getUserView() {
        return uuidIndex.values();
    }
}
