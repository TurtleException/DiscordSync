package de.eldritch.spigot.discord_sync.util.format;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class AttachmentParser {
    public static @NotNull String parseAttachments(List<Message.Attachment> attachments) {
        StringBuilder builder = new StringBuilder("");

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
