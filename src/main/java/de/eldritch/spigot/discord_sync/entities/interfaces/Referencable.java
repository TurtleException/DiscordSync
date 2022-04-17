package de.eldritch.spigot.discord_sync.entities.interfaces;

import org.jetbrains.annotations.NotNull;

/**
 * A message or an object that can be referenced (replied to).
 */
public interface Referencable extends ContainerTextObject, Turtle {
    void setRefNum(@NotNull String refNum);

    @NotNull String getRefNum() throws IllegalStateException;
}
