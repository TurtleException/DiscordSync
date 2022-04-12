package de.eldritch.spigot.discord_sync.sync.listener;

import de.eldritch.spigot.discord_sync.entities.EntityBuilder;
import de.eldritch.spigot.discord_sync.entities.MinecraftMessage;
import de.eldritch.spigot.discord_sync.sync.SynchronizationService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MinecraftChatListener implements Listener {
    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent event) {
        final long timestamp = System.currentTimeMillis();

        if (event == null) return;

        MinecraftMessage message = EntityBuilder.newMinecraftMessage(event.getPlayer().getUniqueId(), event.getMessage(), timestamp);
        SynchronizationService.handle(message);

        // message is sent to minecraft by plugin
        event.setCancelled(true);
    }
}
