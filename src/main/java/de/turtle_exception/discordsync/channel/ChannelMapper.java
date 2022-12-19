package de.turtle_exception.discordsync.channel;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.events.SyncChannelDeleteEvent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelMapper implements Listener {
    private final DiscordSync plugin;

    private @Nullable Channel globalChannel = null;
    private final ConcurrentHashMap<UUID, Channel> channelIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Channel>  playerIndex = new ConcurrentHashMap<>();

    public ChannelMapper(@NotNull DiscordSync plugin) {
        this.plugin = plugin;

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    /* - - - */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerWorldLoad(@NotNull PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World  world  = player.getWorld();

        Channel channel = channelIndex.get(world.getUID());

        playerIndex.put(player.getUniqueId(), channel);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        playerIndex.remove(event.getPlayer().getUniqueId());
        System.out.println("QUIT");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChannelDelete(@NotNull SyncChannelDeleteEvent event) {
        List<UUID> worlds = event.getChannel().getWorlds();

        if (worlds == null) {
            this.globalChannel = null;
        } else {
            for (UUID world : worlds)
                this.channelIndex.remove(world);
        }
    }

    /* - - - */

    public void register(@NotNull Channel channel) {
        this.globalChannel = channel;
    }

    public void register(@NotNull UUID uuid, @NotNull Channel channel) {
        this.channelIndex.put(uuid, channel);
    }

    /* - - - */

    public @NotNull Channel get(@NotNull UUID uuid) {
        Channel channel = channelIndex.get(uuid);
        if (channel != null)
            return channel;
        if (globalChannel == null)
            return Channel.getNullChannel(plugin);
        return globalChannel;
    }

    public @NotNull Channel get(@NotNull Player player) {
        Channel channel = playerIndex.get(player.getUniqueId());
        if (channel == null) {
            channel = this.get(player.getWorld().getUID());
            playerIndex.put(player.getUniqueId(), channel);
        }
        return channel;
    }
}
