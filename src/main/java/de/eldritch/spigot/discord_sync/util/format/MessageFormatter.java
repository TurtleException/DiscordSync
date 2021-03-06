package de.eldritch.spigot.discord_sync.util.format;

import de.eldritch.spigot.discord_sync.entities.DiscordMessage;
import de.eldritch.spigot.discord_sync.entities.MinecraftMessage;
import de.eldritch.spigot.discord_sync.entities.MinecraftSyncMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Util to handle general formatting of messages that need to be translated between services.
 */
public class MessageFormatter {
    /**
     * Provides a formatted representation of the raw message that should be displayed in Minecraft based on the
     * original {@link MinecraftSyncMessage}. This is necessary since the plugin is also responsible for in-game
     * message formatting.
     * @param message The {@link MinecraftSyncMessage} that should be formatted.
     * @return Formatted message for Minecraft.
     */
    public static @NotNull TextComponent formatMinecraft(@NotNull MinecraftSyncMessage message, boolean wrapLine) {
        String str = message.getContent();

        if (message instanceof MinecraftMessage) {
            // remove reference prefix
            if (message.getReference() != null)
                str = str.substring(str.indexOf(" ") + 1);
        }

        // wrap line if it's too long
        if (wrapLine)
            str = WordUtils.wrap(str, 75, "\n", true);

        // allow correct comment formatting in discord
        if (str.stripLeading().startsWith("> "))
            str = "\n" + str.stripLeading();

        TextComponent component = new TextComponent(str);

        component.setColor(ChatColor.GRAY);

        component = MarkdownComponentParser.parse(component);

        if (message instanceof DiscordMessage dMsg) {
            component.addExtra(wrapLine ? "\n" : " ");
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
    public static @NotNull String formatDiscord(@NotNull MinecraftMessage message) {
        String str = message.getContent();

        // remove reference prefix
        if (message.getReference() != null)
            str = str.substring(str.indexOf(" ") + 1);

        // allow correct comment formatting in discord
        if (str.stripLeading().startsWith("> "))
            str = "\n" + str.stripLeading();

        return str;
    }
}
