package de.turtleboi.spigot.dsync.events;

import de.turtleboi.spigot.dsync.SyncUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class SyncUserEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final SyncUser user;

    public SyncUserEvent(final @NotNull SyncUser user) {
        this.user = user;
    }

    public @NotNull SyncUser getUser() {
        return user;
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
