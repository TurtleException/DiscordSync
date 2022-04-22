package de.eldritch.spigot.discord_sync.entities.interfaces;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that can be represented as text in a Minecraft text container.
 * @see HoverEvent
 */
public interface ContainerTextObject {
    /**
     * Provides the text that should be shown in the container.
     * @return Container text.
     */
    @NotNull TextComponent getContainerText();
}
