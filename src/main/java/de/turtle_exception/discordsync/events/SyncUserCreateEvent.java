package de.turtle_exception.discordsync.events;

import de.turtle_exception.discordsync.SyncUser;
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
