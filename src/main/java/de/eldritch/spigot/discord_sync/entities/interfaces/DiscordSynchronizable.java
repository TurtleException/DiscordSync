package de.eldritch.spigot.discord_sync.entities.interfaces;

/**
 * A message or an object that can be represented by a message in Discord, while being from a different origin.
 */
public interface DiscordSynchronizable extends Synchronizable {
    /**
     * Send this object or rather its representation as a message to Discord.
     */
    void sendToDiscord();

    long getSnowflake() throws IllegalStateException;
}
