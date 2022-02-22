package de.eldritch.spigot.discord_sync.sync;

public interface DiscordSynchronizable extends Synchronizable {
    void sendToDiscord();
}
