package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.entities.interfaces.Referencable;
import de.eldritch.spigot.discord_sync.user.User;
import de.eldritch.spigot.discord_sync.util.format.MessageFormatter;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// TODO: Attachments
public class DiscordMessage extends MinecraftSyncMessage {
    private final long snowflake;
    private final List<Message.Attachment> attachments;

    private String format;

    DiscordMessage(long turtle,
                   @NotNull User author,
                   long timestamp,
                   long snowflake,
                   String content,
                   List<Message.Attachment> attachments,
                   @Nullable Referencable reference) {
        super(turtle, author, timestamp, content, reference);

        this.snowflake = snowflake;
        this.attachments = attachments;
    }

    @Override
    public void sendToMinecraft() {
        sendToMinecraft("discord");
    }

    /* ----- ----- ----- */

    @Override
    public @NotNull String getFormat() {
        if (format == null) {
            format = MessageFormatter.format(this);
        }
        return format;
    }

    public long getSnowflake() {
        return snowflake;
    }

    /* ----- ----- ----- */

    public List<Message.Attachment> getAttachments() {
        return attachments;
    }
}
