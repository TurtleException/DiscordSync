package de.turtleboi.spigot.dsync.listeners;

import de.turtleboi.spigot.dsync.DiscordSync;
import de.turtleboi.spigot.dsync.channel.Channel;
import de.turtleboi.spigot.dsync.message.AdvancementMessage;
import de.turtleboi.spigot.dsync.message.DeathMessage;
import de.turtleboi.spigot.dsync.message.JoinMessage;
import de.turtleboi.spigot.dsync.message.QuitMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class EventListener implements Listener {
    private final DiscordSync plugin;

    public EventListener(@NotNull DiscordSync plugin) {
        this.plugin = plugin;
    }

    /* - - - */

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        final long time = System.currentTimeMillis();

        JoinMessage message = new JoinMessage(plugin, time, event.getPlayer());
        Channel     channel = plugin.getChannel(event.getPlayer());

        event.setJoinMessage(null);

        channel.send(message);
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final long time = System.currentTimeMillis();

        QuitMessage message = new QuitMessage(plugin, time, event.getPlayer());
        Channel     channel = plugin.getChannel(event.getPlayer());

        event.setQuitMessage(null);

        channel.send(message);

    }

    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        final long time = System.currentTimeMillis();

        DeathMessage message = new DeathMessage(plugin, time, event.getEntity(), event.getDeathMessage());
        Channel      channel = plugin.getChannel(event.getEntity());

        event.setDeathMessage(null);

        channel.send(message);
    }

    @EventHandler
    public void onPlayerAdvancement(@NotNull PlayerAdvancementDoneEvent event) {
        final long time = System.currentTimeMillis();

        AdvancementMessage message = new AdvancementMessage(plugin, time, event.getPlayer(), event.getAdvancement());
        Channel            channel = plugin.getChannel(event.getPlayer());

        // TODO: should this be cancelled if gamerule announceAdvancements is true?
        channel.send(message);
    }
}
