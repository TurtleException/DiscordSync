package de.turtleboi.spigot.dsync.embedded.listener;

import de.turtleboi.spigot.dsync.embedded.DiscordSyncEmbedded;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.GenericSessionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.Presence;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

public class StatusListener extends ListenerAdapter implements Listener {
    private final DiscordSyncEmbedded plugin;

    public StatusListener(@NotNull DiscordSyncEmbedded plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(@NotNull ServerLoadEvent event) {
        this.updateStatus();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        this.updateStatus();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        // only update status one tick after this event because for some reason spigot still tracks the quitting player
        // as online - even with this listener having MONITOR priority
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, this::updateStatus, 1);
    }

    @Override
    public void onGenericSession(@NotNull GenericSessionEvent event) {
        this.updateStatus();
    }

    private void updateStatus() {
        int players = this.plugin.getServer().getOnlinePlayers().size();

        Activity activity;
        if (players > 0)
            activity = Activity.playing("Minecraft (" + players + ")");
        else
            activity = Activity.playing("Minecraft");

        OnlineStatus status = players > 0 ? OnlineStatus.ONLINE : OnlineStatus.IDLE;

        Presence presence = this.plugin.getJDA().getPresence();
        presence.setPresence(status, activity);
    }
}
