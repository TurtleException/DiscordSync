package de.turtleboi.spigot.dsync.message;

import de.turtleboi.spigot.dsync.DiscordSync;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// unfortunately this can't really be improved because spigot doesn't provide the namespace key with the death event
// thus, the language of the death message (set by server) may differ from the language set in the plugin config
public class DeathMessage extends EventMessage {
    private final Player player;
    private final String message;

    public DeathMessage(@NotNull DiscordSync plugin, long time, @NotNull Player player, String message) {
        super(plugin, time);
        this.player = player;
        this.message = message;
    }

    /* - - - */

    @Override
    public @NotNull String toDiscord(@NotNull MessageChannel recipient) {
        return message;
    }

    @Override
    public @NotNull BaseComponent[] toMinecraft(@NotNull Player recipient) {
        return new BaseComponent[]{ new TextComponent(message) };
    }

    /* - - - */

    public @NotNull Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }
}
