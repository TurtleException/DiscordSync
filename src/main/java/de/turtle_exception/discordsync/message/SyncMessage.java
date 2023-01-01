package de.turtle_exception.discordsync.message;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.SyncUser;
import de.turtle_exception.discordsync.message.source.Author;
import de.turtle_exception.fancyformat.FormatText;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SyncMessage extends MessageEntity {
    private final @NotNull SyncUser author;
    private final @NotNull FormatText content;
    private final long reference;

    public SyncMessage(@NotNull DiscordSync plugin, long id, @NotNull SyncUser author, @NotNull FormatText content, long reference, @NotNull Author source) {
        super(plugin, source, id);
        this.author = author;
        this.content = content;
        this.reference = reference;
    }

    public @NotNull SyncUser getAuthor() {
        return author;
    }

    public @NotNull FormatText getContent() {
        return content;
    }

    @Override
    public @NotNull Author getSource() {
        return (Author) source;
    }

    public long getReference() {
        return reference;
    }

    /* - - - */

    @Override
    public @NotNull String toDiscord(@NotNull MessageChannel recipient) {
        return plugin.getFormatHandler().toDiscord(this, recipient);
    }

    @Override
    public @NotNull BaseComponent[] toMinecraft(@NotNull Player recipient) {
        return plugin.getFormatHandler().toMinecraft(this, recipient);
    }
}
