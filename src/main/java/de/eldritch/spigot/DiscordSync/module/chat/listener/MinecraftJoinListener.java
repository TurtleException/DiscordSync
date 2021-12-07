package de.eldritch.spigot.DiscordSync.module.chat.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinecraftJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(DiscordSync.singleton, () -> MessageService.sendMessage(event.getPlayer(),
                (DiscordSync.singleton.getDiscordAPI() != null && DiscordSync.singleton.getDiscordAPI().getJDA() != null)
                        ? "module.chat.synchronizedJoin" : "module.chat.nonSynchronizedJoin"
        ), 20L);
    }
}