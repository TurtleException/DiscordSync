package de.eldritch.spigot.DiscordSync.module.chat.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinecraftJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(DiscordSync.singleton, () -> event.getPlayer().spigot().sendMessage(
                DiscordSync.singleton.getMessageService().get("general.prefix"),
                DiscordSync.singleton.getMessageService().get(
                        (DiscordSync.singleton.getDiscordAPI() != null && DiscordSync.singleton.getDiscordAPI().getJDA() != null)
                                ? "module.chat.synchronizedJoin" : "module.chat.nonSynchronizedJoin")
        ), 20L);
    }
}