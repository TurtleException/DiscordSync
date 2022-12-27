package de.turtle_exception.discordsync.message;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.Entity;
import de.turtle_exception.discordsync.message.source.Source;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class MessageEntity implements Entity {
    protected final @NotNull DiscordSync plugin;
    protected final @NotNull Source source;

    protected final long id;
    protected final long reference;


    protected MessageEntity(@NotNull DiscordSync plugin, @NotNull Source source, long id, long reference) {
        this.plugin = plugin;
        this.source = source;
        this.id = id;
        this.reference = reference;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getReference() {
        return reference;
    }

    public @NotNull Source getSource() {
        return source;
    }

    /* - - - */

    public abstract @NotNull String toDiscord(@NotNull MessageChannel recipient);

    public abstract @NotNull BaseComponent[] toMinecraft(@NotNull Player recipient);
}
