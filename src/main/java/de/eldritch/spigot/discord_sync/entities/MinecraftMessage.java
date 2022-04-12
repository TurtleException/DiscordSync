package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.entities.interfaces.DiscordSynchronizable;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.User;
import de.eldritch.spigot.discord_sync.util.markdown.MarkdownTranslator;
import net.dv8tion.jda.api.MessageBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinecraftMessage extends MinecraftSyncMessage implements DiscordSynchronizable {
    private long snowflake = -1;

    private String format;

    protected MinecraftMessage(long turtle, @NotNull User author, long timestamp, String content, @Nullable Message reference) {
        super(turtle, author, timestamp, content, reference);
    }

    @Override
    public void sendToDiscord() {
        DiscordSync.singleton.getDiscordService().getAccessor().send(
                Accessor.Channel.MESSAGE,
                new MessageBuilder(
                        Text.of("channel.message", author.getEmote(), getFormat()).content()
                ).build()
        ).queue(
                message -> snowflake = message.getIdLong()
        );
    }

    // NOTE: The plugin is responsible for this to allow formatting and message events (e.g. click-reply)
    @Override
    public void sendToMinecraft() {
        sendToMinecraft("minecraft");
    }

    @Override
    public long getSnowflake() throws IllegalStateException {
        if (snowflake < 0)
            throw new IllegalStateException("Message has not been sent yet.");
        return snowflake;
    }

    /* ----- ----- ----- */

    public @NotNull String getFormat() {
        if (format == null)
            format = MarkdownTranslator.toMarkdown(content);
        return format;
    }
}
