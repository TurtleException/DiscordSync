package de.turtle_exception.discordsync.message;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.fancyformat.FormatText;
import de.turtle_exception.fancyformat.formats.DiscordFormat;
import de.turtle_exception.fancyformat.formats.SpigotComponentsFormat;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class JoinMessage extends EventMessage {
    private final Player player;

    private final FormatText lastOnline;

    public JoinMessage(@NotNull DiscordSync plugin, long time, @NotNull Player player) {
        super(plugin, time);
        this.player = player;

        long last = player.getLastPlayed();
        long diff = time - last;

        if (diff < 0) {
            lastOnline = plugin.getMessageDispatcher().getPlugin("error.lastOnlineFuture", player.getDisplayName());
        } else {
            Duration duration = Duration.ofMillis(diff);

            if (diff < Duration.ofMinutes(1).toMillis())
                lastOnline = plugin.getMessageDispatcher().getPlugin("event.playerJoin.lastOnline.immediate");
            else if (diff < Duration.ofMinutes(2).toMillis())
                lastOnline = plugin.getMessageDispatcher().getPlugin("event.playerJoin.lastOnline.minute");
            else if (diff < Duration.ofHours(2).toMillis())
                lastOnline = plugin.getMessageDispatcher().getPlugin("event.playerJoin.lastOnline.minutes", String.valueOf(duration.toMinutes()));
            else if (diff < Duration.ofDays(2).toMillis())
                lastOnline = plugin.getMessageDispatcher().getPlugin("event.playerJoin.lastOnline.hours", String.valueOf(duration.toHours()));
            else if (diff < Duration.ofDays(60).toMillis())
                lastOnline = plugin.getMessageDispatcher().getPlugin("event.playerJoin.lastOnline.days", String.valueOf(duration.toDays()));
            else
                // not exactly accurate, but treating a month as 30 days is enough
                lastOnline = plugin.getMessageDispatcher().getPlugin("event.playerJoin.lastOnline.months", String.valueOf(duration.toDays() / 30));
        }
    }

    /* - - - */

    @Override
    public @NotNull String toDiscord(@NotNull MessageChannel recipient) {
        String message         = plugin.getMessageDispatcher().getGame("multiplayer.player.joined", player.getDisplayName()).toString(DiscordFormat.get());
        String lastOnlineTitle = plugin.getMessageDispatcher().getPlugin("event.playerJoin.lastOnline").toString(DiscordFormat.get());

        // TODO: embeds

        return message + "\n\n" + lastOnlineTitle + "\n" + lastOnline.toString(DiscordFormat.get());
    }

    @Override
    public @NotNull BaseComponent[] toMinecraft(@NotNull Player recipient) {
        TextComponent comp = new TextComponent(plugin.getMessageDispatcher().getGame("multiplayer.player.joined", player.getDisplayName()).parse(SpigotComponentsFormat.get()));
        comp.setColor(ChatColor.YELLOW);

        return new BaseComponent[]{ comp };
    }

    /* - - - */

    public Player getPlayer() {
        return player;
    }
}
