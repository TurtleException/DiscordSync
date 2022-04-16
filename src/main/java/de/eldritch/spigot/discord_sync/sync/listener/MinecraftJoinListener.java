package de.eldritch.spigot.discord_sync.sync.listener;

import de.eldritch.spigot.discord_sync.DiscordSync;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinecraftJoinListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event == null) return;

        // user will be created if it does not exist yet
        DiscordSync.singleton.getUserService().getUserByUUID(event.getPlayer().getUniqueId());

        DiscordSync.singleton.getAvatarHandler().loadEmote(event.getPlayer());
    }
}
