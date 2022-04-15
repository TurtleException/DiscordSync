package de.eldritch.spigot.discord_sync.util.format;

import de.eldritch.spigot.discord_sync.entities.DiscordMessage;
import de.eldritch.spigot.discord_sync.entities.MinecraftMessage;
import org.jetbrains.annotations.NotNull;

public class MessageFormatter {
    public static @NotNull String format(DiscordMessage message) {
        return MarkdownParser.toLegacyText(message.getContent())
                + AttachmentParser.parseAttachments(message.getAttachments());
    }

    public static @NotNull String format(MinecraftMessage message) {
        return MarkdownParser.toMarkdown(message.getContent());
    }
}
