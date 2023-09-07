package de.turtleboi.spigot.dsync.listeners;

import de.turtleboi.spigot.dsync.DiscordSync;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

public class PresenceListener implements Listener {
    private final DiscordSync plugin;

    public PresenceListener(@NotNull DiscordSync plugin) {
        this.plugin = plugin;
    }

    public void update() {
        int players = plugin.getServer().getOnlinePlayers().size();

        if (players > 0) {
            plugin.getJDA().getPresence().setActivity(Activity.of(Activity.ActivityType.PLAYING, "Minecraft (" + players + ")"));
            plugin.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
        } else {
            plugin.getJDA().getPresence().setActivity(Activity.of(Activity.ActivityType.PLAYING, "Minecraft"));
            plugin.getJDA().getPresence().setStatus(OnlineStatus.IDLE);
        }
    }

    /* - - - */

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        update();
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        update();
    }

    @EventHandler
    public void onPlayerKick(@NotNull PlayerKickEvent event) {
        update();
    }

    @EventHandler
    public void onServerLoad(@NotNull ServerLoadEvent event) {
        update();
    }
}
