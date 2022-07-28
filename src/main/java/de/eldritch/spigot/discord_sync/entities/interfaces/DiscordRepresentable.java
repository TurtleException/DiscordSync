package de.eldritch.spigot.discord_sync.entities.interfaces;

public interface DiscordRepresentable {
    /**
     * Provides the snowflake ID of the Discord message.
     * <p>Implementations might throw {@link IllegalStateException IllegalStateExceptions} if the message does not yet
     * exist.
     * @return Message ID.
     */
    long getSnowflake();
}
