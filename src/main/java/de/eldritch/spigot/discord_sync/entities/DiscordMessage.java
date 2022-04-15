package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.entities.interfaces.Referencable;
import de.eldritch.spigot.discord_sync.user.User;
import de.eldritch.spigot.discord_sync.util.markdown.MarkdownTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: Attachments
public class DiscordMessage extends MinecraftSyncMessage {
    private final long snowflake;

    private String format;

    DiscordMessage(long turtle,
                   @NotNull User author,
                   long timestamp,
                   long snowflake,
                   String content,
                   @Nullable Referencable reference) {
        super(turtle, author, timestamp, content, reference);

        this.snowflake = snowflake;
    }

    @Override
    public void sendToMinecraft() {
        sendToMinecraft("discord");
    }

    /* ----- ----- ----- */

    @Override
    public @NotNull String getFormat() {
        if (format == null)
            format = MarkdownTranslator.toLegacyText(content);
        return format;
    }

    public long getSnowflake() {
        return snowflake;
    }
}
