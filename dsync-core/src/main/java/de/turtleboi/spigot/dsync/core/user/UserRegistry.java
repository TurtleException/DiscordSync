package de.turtleboi.spigot.dsync.core.user;

import de.turtleboi.spigot.dsync.util.CloseableLock;
import de.turtleboi.spigot.dsync.util.IdUtil;
import de.turtleboi.spigot.dsync.util.ReadWriteCloseableLock;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserRegistry {
    private final ReadWriteCloseableLock lock;
    private final ConcurrentHashMap<Long, User> users;

    public UserRegistry() {
        this.lock = new ReadWriteCloseableLock();
        this.users = new ConcurrentHashMap<>();
    }

    public @Nullable User getUser(long snowflake) {
        try (CloseableLock ignored = this.lock.read()) {
            for (User user : this.users.values())
                if (user.hasDiscordAccount(snowflake))
                    return user;
        }
        return null;
    }

    public @Nullable User getUser(@NotNull UUID uuid) {
        try (CloseableLock ignored = this.lock.read()) {
            for (User user : this.users.values())
                if (user.hasMinecraftAccount(uuid))
                    return user;
        }
        return null;
    }

    private @NotNull User createUser(@NotNull Member member) {
        final long   id   = IdUtil.newId((byte) 0);
        final String name = member.getEffectiveName();

        User user = new User(id, name);
        this.addDiscord(user, member.getIdLong());

        this.users.put(id, user);

        return user;
    }

    private @NotNull User createUser(@NotNull OfflinePlayer player) {
        final long   id   = IdUtil.newId((byte) 0);
        final String name = player.getName() != null ? player.getName() : player.getUniqueId().toString();

        User user = new User(id, name);
        this.addMinecraft(user, player.getUniqueId());

        this.users.put(id, user);

        return user;
    }

    public @NotNull User provideUser(@NotNull Member member) {
        try (CloseableLock ignored = this.lock.write()) {
            User user = this.getUser(member.getIdLong());

            if (user != null)
                return user;

            return this.createUser(member);
        }
    }

    public @NotNull User provideUser(@NotNull OfflinePlayer player) {
        try (CloseableLock ignored = this.lock.write()) {
            User user = this.getUser(player.getUniqueId());

            if (user != null)
                return user;

            return this.createUser(player);
        }
    }

    public void addDiscord(@NotNull User user, long snowflake) {
        try (CloseableLock ignored = this.lock.write()) {
            for (User u : this.users.values())
                u.removeDiscord(snowflake);
            user.addDiscord(snowflake);
        }
    }

    public void addMinecraft(@NotNull User user, @NotNull UUID uuid) {
        try (CloseableLock ignored = this.lock.write()) {
            for (User u : this.users.values())
                u.removeMinecraft(uuid);
            user.removeMinecraft(uuid);
        }
    }
}
