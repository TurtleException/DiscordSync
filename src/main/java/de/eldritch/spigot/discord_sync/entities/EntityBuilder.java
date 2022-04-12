package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.sync.ReferenceHelper;
import de.eldritch.spigot.discord_sync.user.User;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.utils.TimeUtil;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

// TODO: docs
public class EntityBuilder {
    public static DiscordMessage newDiscordMessage(@NotNull net.dv8tion.jda.api.entities.Message message) throws UnsupportedOperationException, IllegalStateException {
        return new DiscordMessage(
                TurtleBuilder.newID(),
                getUser(message.getAuthor().getIdLong()),
                getTimestamp(message),
                message.getIdLong(),
                message.getContentRaw(),
                ReferenceHelper.getReference(message)
        );
    }

    public static MinecraftMessage newMinecraftMessage(@NotNull UUID author, String content, long timestamp) {
        return new MinecraftMessage(
                TurtleBuilder.newID(),
                getUser(author),
                timestamp,
                content,
                ReferenceHelper.getReference(content)
        );
    }

    public static MinecraftAdvancementEvent newMinecraftAdvancementEvent(@NotNull UUID player, Advancement advancement, long timestamp) {
        return new MinecraftAdvancementEvent(
                timestamp,
                getUser(player),
                advancement
        );
    }

    public static MinecraftDeathEvent newMinecraftDeathEvent(@NotNull UUID player, long timestamp, String message, Collection<? extends Player> onlinePlayers) {
        return new MinecraftDeathEvent(
                timestamp,
                getUser(player),
                message,
                onlinePlayers
        );
    }

    public static MinecraftJoinEvent newMinecraftJoinEvent(@NotNull UUID player, long timestamp, long last) {
        return new MinecraftJoinEvent(
                timestamp,
                getUser(player),
                last
        );
    }

    public static MinecraftQuitEvent newMinecraftQuitEvent(@NotNull UUID player, long timestamp) {
        return new MinecraftQuitEvent(
                timestamp,
                getUser(player)
        );
    }

    /* ----- ----- ----- */

    /**
     * Responsible for creating new Turtle IDs.
     */
    public static class TurtleBuilder {
        private static long latest = 0;

        /**
         * Provides a new unique Turtle ID.
         * @return New Turtle ID.
         */
        public static synchronized long newID() {
            long current = System.currentTimeMillis();
            // increment if ID is already in use
            latest = current == latest ? current++ : current;
            return current;
        }
    }

    public static @NotNull User getUser(long snowflake) {
        return DiscordSync.singleton.getUserService().getUserBySnowflake(snowflake);
    }

    public static @NotNull User getUser(@NotNull UUID uuid) {
        return DiscordSync.singleton.getUserService().getUserByUUID(uuid);
    }

    public static long getTimestamp(long snowflake) {
        return (snowflake >>> TimeUtil.TIMESTAMP_OFFSET) + TimeUtil.DISCORD_EPOCH;
    }

    public static long getTimestamp(@NotNull ISnowflake entity) {
        return getTimestamp(entity.getIdLong());
    }
}
