package de.eldritch.spigot.discord_sync.entities.interfaces;

import de.eldritch.spigot.discord_sync.text.Text;
import net.md_5.bungee.api.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that can be represented as text in a Minecraft text container.
 * @see HoverEvent
 */
public interface ContainerTextObject {
    @NotNull Text getContainerText();
}
