package de.turtle_exception.discordsync.message;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.fancyformat.formats.DiscordFormat;
import de.turtle_exception.fancyformat.formats.SpigotComponentsFormat;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuitMessage extends EventMessage {
    private final Player player;

    public QuitMessage(@NotNull DiscordSync plugin, long time, @NotNull Player player) {
        super(plugin, time);
        this.player = player;
    }

    /* - - - */

    @Override
    public @NotNull String toDiscord(@NotNull MessageChannel recipient) {
        return plugin.getMessageDispatcher().get("multiplayer.player.left", player.getDisplayName()).toString(DiscordFormat.get());
    }

    @Override
    public @NotNull BaseComponent[] toMinecraft(@NotNull Player recipient) {
        TextComponent comp = new TextComponent(plugin.getMessageDispatcher().get("multiplayer.player.left", player.getDisplayName()).parse(SpigotComponentsFormat.get()));
        comp.setColor(ChatColor.YELLOW);

        return new BaseComponent[]{ comp };
    }

    /* - - - */

    public Player getPlayer() {
        return player;
    }
}
