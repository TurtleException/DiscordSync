package de.turtleboi.spigot.dsync.events;

import de.turtleboi.spigot.dsync.channel.Channel;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SyncChannelCreateEvent extends SyncChannelEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public SyncChannelCreateEvent(final @NotNull Channel channel) {
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
