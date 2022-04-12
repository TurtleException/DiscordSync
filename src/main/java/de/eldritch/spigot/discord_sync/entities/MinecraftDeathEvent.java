package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MinecraftDeathEvent extends MinecraftEvent {
    private final String message;

    public MinecraftDeathEvent(long timestamp, @NotNull User user, String message, Collection<? extends Player> onlinePlayers) {
        super(timestamp, Accessor.Channel.DEATH, user);

        this.message = formatMessage(message, onlinePlayers);

        this.initBuilder();
    }

    @Override
    protected void initBuilder() {
        builder = user.newEmbed().setDescription(message);
    }

    private String formatMessage(String msg, Collection<? extends Player> onlinePlayers) {
        String newMsg = msg;
        for (Player player : onlinePlayers) {
            if (newMsg.contains(player.getName())) {
                final User otherUser = DiscordSync.singleton.getUserService().getUserByUUID(player.getUniqueId());
                newMsg = newMsg.replaceAll(player.getName(), otherUser.getMention());
            }
        }
        return newMsg;
    }
}
