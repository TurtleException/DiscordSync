package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.entities.interfaces.Referencable;
import de.eldritch.spigot.discord_sync.user.User;
import de.eldritch.spigot.discord_sync.util.markdown.MarkdownTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiscordMessage extends MinecraftSyncMessage {
    /* SNOWFLAKE IDS */
    private final long sfGuild;
    private final long sfChannel;
    private final long sfMessage;

    private String format;

    DiscordMessage(long turtle,
                   @NotNull User author,
                   long timestamp,
                   long sfGuild,
                   long sfChannel,
                   long sfMessage,
                   String content,
                   @Nullable Referencable reference) {
        super(turtle, author, timestamp, content, reference);

        this.sfGuild = sfGuild;
        this.sfChannel = sfChannel;
        this.sfMessage = sfMessage;
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
        return sfMessage;
    }
}