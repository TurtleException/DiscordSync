package de.turtleboi.spigot.dsync.events;

import de.turtleboi.spigot.dsync.SyncUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class SyncUserCreateEvent extends SyncUserEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public SyncUserCreateEvent(final @NotNull SyncUser user) {
        super(user);
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
