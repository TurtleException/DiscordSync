package de.eldritch.spigot.discord_sync.entities.interfaces;

import de.eldritch.spigot.discord_sync.sync.SynchronizationService;
import org.jetbrains.annotations.NotNull;

/**
 * A message or an object that can be referenced (replied to).
 */
public interface Referencable extends ContainerTextObject, Turtle {
    void setRefNum(@NotNull String refNum);

    /**
     * Provides the quick reference number that can be used to reply to this message.
     * @return Quick reference number of this message.
     * @throws IllegalStateException if this message has not yet been handled by the {@link SynchronizationService} and
     *                               thus does not have a reference number yet.
     * @see SynchronizationService#handle(Synchronizable)
     * @see SynchronizationService#getCachedReferencable(String)
     */
    @NotNull String getRefNum() throws IllegalStateException;
}
