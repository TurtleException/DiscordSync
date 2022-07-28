package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.user.User;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MinecraftDeathEvent extends MinecraftEvent {
    public MinecraftDeathEvent(long timestamp, @NotNull User user, String message, Collection<? extends Player> onlinePlayers) {
        super(timestamp, Accessor.Channel.DEATH, user, initBuilder(user, formatMessage(message, onlinePlayers)));
    }

    private static EmbedBuilder initBuilder(User user, String message) {
        return user.newEmbed().setDescription(message);
    }

    private static String formatMessage(String msg, Collection<? extends Player> onlinePlayers) {
        String newMsg = msg;
        for (Player player : onlinePlayers) {
            if (newMsg.contains(player.getName())) {
                final User otherUser = DiscordSync.singleton.getUserService().ofUUID(player.getUniqueId());
                newMsg = newMsg.replaceAll(player.getName(), otherUser.getMention());
            }
        }
        return newMsg;
    }
}
