package de.turtleboi.spigot.dsync.channel.endpoints;

import de.turtleboi.spigot.dsync.channel.Channel;
import de.turtleboi.spigot.dsync.channel.Endpoint;
import de.turtleboi.spigot.dsync.message.MessageEntity;
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
    public void send(@NotNull MessageEntity message) {
        for (Player player : this.getPlayers())
            player.spigot().sendMessage(message.toMinecraft(player));
    }

    public abstract Collection<? extends Player> getPlayers();
}
