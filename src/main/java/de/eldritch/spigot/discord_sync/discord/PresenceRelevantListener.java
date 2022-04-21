package de.eldritch.spigot.discord_sync.discord;

import de.eldritch.spigot.discord_sync.DiscordSync;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PresenceRelevantListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event == null) return;

        PresenceHandler.update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event == null) return;

        DiscordSync.singleton.getServer().getScheduler().runTaskLater(DiscordSync.singleton, PresenceHandler::update, 5L);
    }
}
