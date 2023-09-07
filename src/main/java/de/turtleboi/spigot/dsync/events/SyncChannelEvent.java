package de.turtleboi.spigot.dsync.events;

import de.turtleboi.spigot.dsync.channel.Channel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class SyncChannelEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Channel channel;

    public SyncChannelEvent(final @NotNull Channel channel) {
        this.channel = channel;
    }

    public @NotNull Channel getChannel() {
        return channel;
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
