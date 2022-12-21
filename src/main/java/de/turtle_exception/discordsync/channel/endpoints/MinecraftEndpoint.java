package de.turtle_exception.discordsync.channel.endpoints;

import de.turtle_exception.discordsync.SyncMessage;
import de.turtle_exception.discordsync.channel.Channel;
import de.turtle_exception.discordsync.channel.Endpoint;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class MinecraftEndpoint extends Endpoint {
    protected MinecraftEndpoint(long id, @NotNull Channel channel) {
        super(id, channel);
    }

    public MinecraftEndpoint(@NotNull Channel channel) {
        super(channel);
    }

    @Override
    public void send(@NotNull SyncMessage message) {
        for (Player player : this.getPlayers())
            player.spigot().sendMessage(channel.getPlugin().getFormatHandler().toMinecraft(message, player));
    }

    public abstract Collection<? extends Player> getPlayers();
}
