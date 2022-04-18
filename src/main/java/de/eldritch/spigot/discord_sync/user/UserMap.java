package de.eldritch.spigot.discord_sync.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserMap {
    private final ConcurrentHashMap<Long, User> userMap_turtle    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, User> userMap_uuid      = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, User> userMap_snowflake = new ConcurrentHashMap<>();

    public @Nullable User getByTurtle(long turtle) {
        return userMap_turtle.get(turtle);
    }

    public @Nullable User getByUUID(@NotNull UUID uuid) {
        return userMap_uuid.get(uuid);
    }

    public @Nullable User getBySnowflake(long snowflake) {
        return userMap_snowflake.get(snowflake);
    }

    public void put(@NotNull User user) {
        if (user.minecraftOffline() != null)
            userMap_uuid.put(user.minecraftOffline().getUniqueId(), user);
        if (user.discord() != null)
            userMap_snowflake.put(user.discord().getIdLong(), user);

        userMap_turtle.put(user.getID(), user);
    }

    public void remove(@NotNull User user) {
        if (user.minecraftOffline() != null)
            userMap_uuid.remove(user.minecraftOffline().getUniqueId());
        if (user.discord() != null)
            userMap_snowflake.remove(user.discord().getIdLong());

        userMap_turtle.remove(user.getID());
    }

    /* ----- ----- ----- */

    public void updateIndexUUID(@Nullable UUID oldIndex, @Nullable UUID newIndex, @NotNull User user) {
        User oldUser = null;

        if (oldIndex != null)
            userMap_uuid.remove(oldIndex, user);
        if (newIndex != null)
            oldUser = userMap_uuid.put(newIndex, user);

        if (oldUser != null)
            remove(oldUser);
    }

    public void updateIndexSnowflake(@Nullable Long oldIndex, @Nullable Long newIndex, @NotNull User user) {
        User oldUser = null;

        if (oldIndex != null)
            userMap_snowflake.remove(oldIndex, user);
        if (newIndex != null)
            oldUser = userMap_snowflake.put(newIndex, user);

        if (oldUser != null)
            remove(oldUser);
    }

    /* ----- ----- ----- */

    public @NotNull Set<User> getView() {
        Collection<User> users = userMap_turtle.values();
        return Set.copyOf(!users.isEmpty() ? users : Set.of());
    }
}
