package de.turtleboi.spigot.dsync.channel.endpoints;

import de.turtleboi.spigot.dsync.channel.Channel;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MinecraftWorld extends MinecraftEndpoint {
    private final @NotNull UUID uuid;

    public MinecraftWorld(long id, @NotNull Channel channel, @NotNull UUID uuid) {
        super(id, channel);
        this.uuid = uuid;
    }

    public MinecraftWorld(@NotNull Channel channel, @NotNull UUID uuid) {
        super(channel);
        this.uuid = uuid;
    }

    public @NotNull UUID getUUID() {
        return uuid;
    }

    @Override
    public Collection<? extends Player> getPlayers() {
        World world = channel.getPlugin().getServer().getWorld(uuid);

        // TODO: should this be handled?
        if (world == null) return List.of();

        return world.getPlayers();
    }
}
