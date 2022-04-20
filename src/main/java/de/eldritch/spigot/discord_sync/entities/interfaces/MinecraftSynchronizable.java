package de.eldritch.spigot.discord_sync.entities.interfaces;

/**
 * A message or an object that can be represented by a message in Minecraft, while being from a different origin.
 */
public interface MinecraftSynchronizable extends Synchronizable {
    /**
     * Send this object or rather its representation as a message to Minecraft.
     */
    void sendToMinecraft();
}
