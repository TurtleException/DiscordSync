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

public class EntityBuilder {
    /**
     * Constructs a new {@link DiscordMessage} from a {@link net.dv8tion.jda.api.entities.Message Message}. The TurtleID
     * will be newly created and the {@link User} association will be managed automatically.
     * @param message The Discord message.
     * @return A new {@link DiscordMessage} object.
     */
    public static DiscordMessage newDiscordMessage(@NotNull net.dv8tion.jda.api.entities.Message message) throws UnsupportedOperationException, IllegalStateException {
        return new DiscordMessage(
                TurtleBuilder.newID(),
                getUser(message.getAuthor().getIdLong()),
                getTimestamp(message),
                message.getIdLong(),
                message.getContentRaw(),
                message.getAttachments(),
                ReferenceHelper.getReference(message)
        );
    }

    /**
     * Constructs a new {@link MinecraftMessage} from a chat message on the server. The TurtleID will be newly created
     * and the {@link User} association will be managed automatically.
     * @param author UUID of the player that sent the message.
     * @param content The legacy text content of the message.
     * @param timestamp The exact time the message has been received by the EventHandler.
     * @return A new {@link MinecraftMessage} object.
     */
    public static MinecraftMessage newMinecraftMessage(@NotNull UUID author, String content, long timestamp) {
        return new MinecraftMessage(
                TurtleBuilder.newID(),
                getUser(author),
                timestamp,
                content,
                ReferenceHelper.getReference(content)
        );
    }

    /**
     * Constructs a new {@link MinecraftAdvancementEvent} from an {@link Advancement}. The TurtleID will be newly
     * created and the {@link User} association will be managed automatically.
     * @param player UUID of the player that achieved the advancement.
     * @param advancement The advancement that the player achieved.
     * @param timestamp The exact time the event has been received by the EventHandler.
     * @return A new {@link MinecraftAdvancementEvent} object.
     */
    public static MinecraftAdvancementEvent newMinecraftAdvancementEvent(@NotNull UUID player, Advancement advancement, long timestamp) {
        return new MinecraftAdvancementEvent(
                timestamp,
                getUser(player),
                advancement
        );
    }

    /**
     * Constructs a new {@link MinecraftDeathEvent} from a player death. The TurtleID will be newly created and the
     * {@link User} association will be managed automatically.
     * @param player UUID of the player that died.
     * @param timestamp The exact time the event has been received by the EventHandler.
     * @param message The death message
     * @param onlinePlayers Collection of players that were online during the event.
     * @return A new {@link MinecraftDeathEvent} object.
     */
    public static MinecraftDeathEvent newMinecraftDeathEvent(@NotNull UUID player, long timestamp, String message, Collection<? extends Player> onlinePlayers) {
        return new MinecraftDeathEvent(
                timestamp,
                getUser(player),
                message,
                onlinePlayers
        );
    }

    /**
     * Constructs a new {@link MinecraftJoinEvent} from a player join. The TurtleID will be newly created and the
     * {@link User} association will be managed automatically.
     * @param player UUID of the player that joined.
     * @param timestamp The exact time the event has been received by the EventHandler.
     * @param last UNIX time of the last time the player was online.
     * @return A new {@link MinecraftJoinEvent} object.
     */
    public static MinecraftJoinEvent newMinecraftJoinEvent(@NotNull UUID player, long timestamp, long last) {
        return new MinecraftJoinEvent(
                timestamp,
                getUser(player),
                last
        );
    }

    /**
     * Constructs a new {@link MinecraftQuitEvent} from a player quit. The TurtleID will be newly created and the
     * {@link User} association will be managed automatically.
     * @param player UUID of the player that quit.
     * @param timestamp The exact time the event has been received by the EventHandler.
     * @return A new {@link MinecraftQuitEvent} object.
     */
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
        return DiscordSync.singleton.getUserService().getBySnowflake(snowflake);
    }

    public static @NotNull User getUser(@NotNull UUID uuid) {
        return DiscordSync.singleton.getUserService().getByUUID(uuid);
    }

    public static long getTimestamp(long snowflake) {
        return (snowflake >>> TimeUtil.TIMESTAMP_OFFSET) + TimeUtil.DISCORD_EPOCH;
    }

    public static long getTimestamp(@NotNull ISnowflake entity) {
        return getTimestamp(entity.getIdLong());
    }
}
