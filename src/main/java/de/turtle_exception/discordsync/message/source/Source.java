package de.turtle_exception.discordsync.message.source;

public abstract class Source {
    public boolean isFromDiscordChannel(long snowflake) {
        return false;
    }
}
