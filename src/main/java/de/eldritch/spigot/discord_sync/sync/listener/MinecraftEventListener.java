package de.eldritch.spigot.discord_sync.sync.listener;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.entities.*;
import de.eldritch.spigot.discord_sync.sync.SynchronizationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;

public class MinecraftEventListener implements Listener {
    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        final long timestamp = System.currentTimeMillis();

        if (event == null) return;
        if (event.getAdvancement().getKey().getKey().startsWith("recipes")) return;

        MinecraftAdvancementEvent syncEvent = EntityBuilder.newMinecraftAdvancementEvent(event.getPlayer().getUniqueId(), event.getAdvancement(), timestamp);
        SynchronizationService.handle(syncEvent);
    }

    /**
     * Note: I hate spigot for not providing a namespace key here :c
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final long timestamp = System.currentTimeMillis();

        if (event == null) return;

        final Collection<? extends Player> onlinePlayers = DiscordSync.singleton.getServer().getOnlinePlayers();

        MinecraftDeathEvent syncEvent = EntityBuilder.newMinecraftDeathEvent(event.getEntity().getUniqueId(), timestamp, event.getDeathMessage(), onlinePlayers);
        SynchronizationService.handle(syncEvent);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final long timestamp = System.currentTimeMillis();

        if (event == null) return;

        MinecraftJoinEvent syncEvent = EntityBuilder.newMinecraftJoinEvent(event.getPlayer().getUniqueId(), timestamp, event.getPlayer().getLastPlayed());
        SynchronizationService.handle(syncEvent);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final long timestamp = System.currentTimeMillis();

        if (event == null) return;

        MinecraftQuitEvent syncEvent = EntityBuilder.newMinecraftQuitEvent(event.getPlayer().getUniqueId(), timestamp);
        SynchronizationService.handle(syncEvent);
    }
}
