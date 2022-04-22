package de.eldritch.spigot.discord_sync.util.format;

import de.eldritch.spigot.discord_sync.entities.DiscordMessage;
import de.eldritch.spigot.discord_sync.entities.MinecraftMessage;
import de.eldritch.spigot.discord_sync.entities.MinecraftSyncMessage;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Util to handle general formatting of messages that need to be translated between services.
 */
public class MessageFormatter {
    /**
     * Provides a formatted representation of the raw message that should be displayed in Minecraft based on the
     * original {@link DiscordMessage}.
     * @param message The DiscordMessage that should be formatted.
     * @return Formatted message for Minecraft.
     */
    public static @NotNull TextComponent formatMinecraft(DiscordMessage message) {
        String str = message.getContent();

        TextComponent component = new TextComponent(message.getContent());

        component = MarkdownParser.toComponent(component);

        str = MarkdownParser.toLegacyText(str);
        str = str + AttachmentParser.parseEmbeds(message.getEmbeds());
        str = str + AttachmentParser.parseAttachments(message.getAttachments());

        return str;
    }

    /**
     * Provides a formatted representation of the raw message that should be displayed in Minecraft based on the
     * original {@link MinecraftSyncMessage}. This is necessary since the plugin is also responsible for in-game
     * message formatting.
     * @param message The {@link MinecraftSyncMessage} that should be formatted.
     * @return Formatted message for Minecraft.
     */
    public static @NotNull TextComponent formatMinecraft(MinecraftSyncMessage message) {
        String str = message.getContent();

        // remove reference prefix
        if (message.getReference() != null)
            str = str.substring(str.indexOf(" ") + 1);

        TextComponent component = new TextComponent(str);

        component = MarkdownComponentParser.parse(component);

        if (message instanceof DiscordMessage dMsg) {
            component.addExtra(AttachmentParser.parseEmbeds(dMsg.getEmbeds()));
            component.addExtra(AttachmentParser.parseAttachments(dMsg.getAttachments()));
        }

        return component;
    }

    /**
     * Provides a formatted representation of the raw message that should be displayed in Discord based on the original
     * {@link MinecraftMessage}.
     * @param message The MinecraftMessage that should be formatted.
     * @return Formatted message for Discord.
     */
    public static @NotNull String formatDiscord(MinecraftMessage message) {
        String str = message.getContent();

        // remove reference prefix
        if (message.getReference() != null)
            str = str.substring(str.indexOf(" ") + 1);

        str = MarkdownParser.toMarkdown(str);

        return str;
    }
}
