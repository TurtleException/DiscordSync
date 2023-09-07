package de.turtleboi.spigot.dsync.channel;

import de.turtleboi.spigot.dsync.Entity;
import de.turtleboi.spigot.dsync.message.MessageEntity;
import de.turtleboi.spigot.dsync.util.time.TurtleType;
import de.turtleboi.spigot.dsync.util.time.TurtleUtil;
import org.jetbrains.annotations.NotNull;

public abstract class Endpoint implements Entity {
    protected final long id;

    protected final @NotNull Channel channel;

    protected Endpoint(long id, @NotNull Channel channel) {
        this.id = id;
        this.channel = channel;
    }

    public Endpoint(@NotNull Channel channel) {
        this(TurtleUtil.newId(TurtleType.ENDPOINT), channel);
    }

    @Override
    public long getId() {
        return id;
    }

    public abstract void send(@NotNull MessageEntity message);
}
