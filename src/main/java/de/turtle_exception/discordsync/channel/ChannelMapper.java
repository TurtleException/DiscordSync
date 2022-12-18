package de.turtle_exception.discordsync.channel;

import de.turtle_exception.discordsync.DiscordSync;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelMapper {
    private final DiscordSync plugin;

    private final ConcurrentHashMap<UUID, Long> map = new ConcurrentHashMap<>();

    public ChannelMapper(@NotNull DiscordSync plugin) {
        this.plugin = plugin;
    }

    public @NotNull Channel get(@NotNull UUID uuid) {
        Long id = map.get(uuid);
        if (id == null)
            return Channel.getNullChannel(plugin);

        Channel channel = plugin.getChannelCache().get(id);
        if (channel == null)
            return Channel.getNullChannel(plugin);

        return channel;
    }

    public void set(@NotNull UUID uuid, @NotNull Long channel) {
        this.map.put(uuid, channel);
    }
}
