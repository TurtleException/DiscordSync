package de.turtleboi.spigot.dsync.message.source;

public abstract class Source {
    public boolean isFromDiscordChannel(long snowflake) {
        return false;
    }
}