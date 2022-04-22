package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.entities.interfaces.DiscordRepresentable;
import de.eldritch.spigot.discord_sync.entities.interfaces.Referencable;
import de.eldritch.spigot.discord_sync.user.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DiscordMessage extends MinecraftSyncMessage implements DiscordRepresentable {
    private final long snowflake;
    private final List<MessageEmbed> embeds;
    private final List<Message.Attachment> attachments;

    DiscordMessage(long turtle,
                   @NotNull User author,
                   long timestamp,
                   long snowflake,
                   String content,
                   List<MessageEmbed> embeds,
                   List<Message.Attachment> attachments,
                   @Nullable Referencable reference) {
        super(turtle, author, timestamp, content, reference);

        this.snowflake = snowflake;

        this.embeds = embeds;
        this.attachments = attachments;
    }

    @Override
    public String getLogMessage() {
        final String refPrefix = reference != null
                ? "@" + reference.getRefNum() + " "
                : "";

        return "[%s] [DISCORD] <%s> %s".formatted(refNum, author.getEffectiveName(), refPrefix + content);
    }

    @Override
    public void sendToMinecraft() {
        sendToMinecraft("discord");
    }

    /* ----- ----- ----- */

    @Override
    public long getSnowflake() {
        return snowflake;
    }

    /* ----- ----- ----- */

    public List<MessageEmbed> getEmbeds() {
        return embeds;
    }

    public List<Message.Attachment> getAttachments() {
        return attachments;
    }
}
