package de.turtle_exception.discordsync.message;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.message.source.ServerSource;
import de.turtle_exception.discordsync.util.time.TurtleType;
import de.turtle_exception.discordsync.util.time.TurtleUtil;
import org.jetbrains.annotations.NotNull;

public abstract class EventMessage extends MessageEntity {
    private final long time;

    public EventMessage(@NotNull DiscordSync plugin, long time) {
        super(plugin, ServerSource.get(), TurtleUtil.newId(TurtleType.MESSAGE));
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
