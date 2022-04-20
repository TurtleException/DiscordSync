package de.eldritch.spigot.discord_sync.util.format;

import de.eldritch.spigot.discord_sync.entities.DiscordMessage;
import de.eldritch.spigot.discord_sync.entities.MinecraftMessage;
import de.eldritch.spigot.discord_sync.entities.MinecraftSyncMessage;
import org.jetbrains.annotations.NotNull;

public class MessageFormatter {
    public static @NotNull String formatMinecraft(DiscordMessage message) {
        String str = message.getContent();

        str = MarkdownParser.toLegacyText(str);
        str = str + AttachmentParser.parseAttachments(message.getAttachments());

        return str;
    }

    public static @NotNull String formatMinecraft(MinecraftSyncMessage message) {
        String str = message.getContent();

        // remove reference prefix
        if (message.getReference() != null)
            str = str.substring(str.indexOf(" ") + 1);

        str = MarkdownParser.toLegacyText(str);

        return str;
    }

    public static @NotNull String formatDiscord(MinecraftMessage message) {
        String str = message.getContent();

        // remove reference prefix
        if (message.getReference() != null)
            str = str.substring(str.indexOf(" ") + 1);

        str = MarkdownParser.toMarkdown(str);

        return str;
    }
}
