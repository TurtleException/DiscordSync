package de.turtle_exception.discordsync.channel.endpoints;

import de.turtle_exception.discordsync.channel.Channel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class MinecraftServer extends MinecraftEndpoint {
    public MinecraftServer(long id, @NotNull Channel channel) {
        super(id, channel);
    }

    public MinecraftServer(@NotNull Channel channel) {
        super(channel);
    }

    @Override
    public Collection<? extends Player> getPlayers() {
        // TODO: does this have to be a copy?
        return List.copyOf(channel.getPlugin().getServer().getOnlinePlayers());
    }
}
