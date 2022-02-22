package de.eldritch.spigot.discord_sync.chat;

import de.eldritch.spigot.discord_sync.user.User;
import org.jetbrains.annotations.NotNull;

public abstract class Message {
    protected final User author;

    // UNIX time in millis
    protected final long timestamp;

    protected Message(@NotNull User author, long timestamp) {
        this.author = author;
        this.timestamp = timestamp;
    }
}
