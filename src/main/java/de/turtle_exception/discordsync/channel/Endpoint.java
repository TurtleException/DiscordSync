package de.turtle_exception.discordsync.channel;

import de.turtle_exception.discordsync.Entity;
import de.turtle_exception.discordsync.SyncMessage;
import de.turtle_exception.discordsync.util.time.TurtleType;
import de.turtle_exception.discordsync.util.time.TurtleUtil;
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

    public abstract void send(@NotNull SyncMessage message);
}
