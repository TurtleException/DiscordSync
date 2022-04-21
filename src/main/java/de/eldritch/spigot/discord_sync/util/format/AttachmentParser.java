package de.eldritch.spigot.discord_sync.util.format;

import de.eldritch.spigot.discord_sync.entities.DiscordMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Util to handle Discord Attachments and their respective display in Minecraft.
 */
class AttachmentParser {
    public static @NotNull String parseEmbeds(List<MessageEmbed> embeds) {
        StringBuilder builder = new StringBuilder();

        for (MessageEmbed embed : embeds) {
            builder.append(" &8[EMBED]");
        }

        return builder.toString();
    }

    /**
     * Returns a formatted message suffix to append to a {@link DiscordMessage} in Minecraft containing information
     * about {@link Message.Attachment Attachments}.
     * @param attachments List of {@link Message.Attachment Attachments}
     * @return Formatted message suffix.
     */
    public static @NotNull String parseAttachments(List<Message.Attachment> attachments) {
        StringBuilder builder = new StringBuilder();

        boolean image = false;
        boolean video = false;
        boolean file  = false;

        for (Message.Attachment attachment : attachments) {
            if (attachment.isImage())
                image = true;
            else if (attachment.isVideo())
                video = true;
            else
                file  = true;
        }

        if (image)
            builder.append(" &8[IMG]");
        if (video)
            builder.append(" &8[VID]");
        if (file)
            builder.append(" &8[FILE]");

        return ColorParser.format(builder.toString());
    }
}
