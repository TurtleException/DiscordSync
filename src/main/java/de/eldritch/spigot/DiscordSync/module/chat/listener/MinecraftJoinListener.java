package de.eldritch.spigot.DiscordSync.module.chat.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinecraftJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ComponentBuilder builder = new ComponentBuilder(DiscordSync.getChatPrefix());

        builder.append((DiscordSync.singleton.getDiscordAPI() != null && DiscordSync.singleton.getDiscordAPI().getJDA() != null)
                        ? "Discord-Chat synchronisiert."
                        : "Discord-Chat nicht synchronisiert.")
                .color(ChatColor.GRAY);

        Bukkit.getScheduler().runTaskLaterAsynchronously(DiscordSync.singleton, () ->
                event.getPlayer().spigot().sendMessage(builder.create()), 20L);
    }
}