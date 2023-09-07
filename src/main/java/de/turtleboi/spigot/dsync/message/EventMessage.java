package de.turtleboi.spigot.dsync.message;

import de.turtleboi.spigot.dsync.DiscordSync;
import de.turtleboi.spigot.dsync.message.source.ServerSource;
import de.turtleboi.spigot.dsync.util.time.TurtleType;
import de.turtleboi.spigot.dsync.util.time.TurtleUtil;
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
