package de.turtle_exception.discordsync.events;

import de.turtle_exception.discordsync.channel.Channel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SyncChannelDeleteEvent extends SyncChannelEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public SyncChannelDeleteEvent(final @NotNull Channel channel) {
        super(channel);
    }

    /* - - - */

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
