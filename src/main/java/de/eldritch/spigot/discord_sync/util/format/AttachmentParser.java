package de.eldritch.spigot.discord_sync.util.format;

import de.eldritch.spigot.discord_sync.entities.DiscordMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Util to handle Discord Attachments and their respective display in Minecraft.
 */
class AttachmentParser {
    public static @NotNull TextComponent parseEmbeds(List<MessageEmbed> embeds) {
        if (embeds.size() < 1)
            return new TextComponent();

        TextComponent component = new TextComponent();

        component.setText("[EMBED] ");
        component.setColor(ChatColor.DARK_GRAY);

        return component;
    }

    /**
     * Returns a formatted message suffix to append to a {@link DiscordMessage} in Minecraft containing information
     * about {@link Message.Attachment Attachments}.
     * @param attachments List of {@link Message.Attachment Attachments}
     * @return Formatted message suffix.
     */
    public static @NotNull TextComponent parseAttachments(List<Message.Attachment> attachments) {
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
            builder.append("[IMG] ");
        if (video)
            builder.append("[VID] ");
        if (file)
            builder.append("[FILE] ");

        TextComponent component = new TextComponent();

        component.setText(builder.toString());
        component.setColor(ChatColor.DARK_GRAY);

        return component;
    }
}
