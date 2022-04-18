package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.entities.interfaces.DiscordRepresentable;
import de.eldritch.spigot.discord_sync.entities.interfaces.Referencable;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.LegacyUser;
import de.eldritch.spigot.discord_sync.util.format.MessageFormatter;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DiscordMessage extends MinecraftSyncMessage implements DiscordRepresentable {
    private final long snowflake;
    private final List<Message.Attachment> attachments;

    DiscordMessage(long turtle,
                   @NotNull LegacyUser author,
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

    @Override
    public @NotNull Text getContainerText() {
        return Text.of("chat.reference.container", author.getEffectiveName(), String.valueOf(getID()), MessageFormatter.formatMinecraft(this));
    }

    /* ----- ----- ----- */

    @Override
    public long getSnowflake() {
        return snowflake;
    }

    /* ----- ----- ----- */

    public List<Message.Attachment> getAttachments() {
        return attachments;
    }
}
