package de.eldritch.spigot.discord_sync.util.text;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public record Text(@NotNull String content) {
    public TextComponent toBaseComponent() {
        return new TextComponent(TextComponent.fromLegacyText(content));
    }

    public TextComponent toBaseComponent(ChatColor defaultColor) {
        return new TextComponent(TextComponent.fromLegacyText(content, defaultColor));
    }

    /* ------------------------- */

    /**
     * Provides a {@link Text} with its {@link String} content taken from a language file.
     * @param key Key to the String stored in a language file.
     * @param format Arguments to use as replacement for the format specified in the String.
     * @return Formatted {@link Text} from the specified language and its key.
     * @throws NullPointerException if the key does not point to a valid String.
     * @see TextUtil#load(String)
     */
    public static @NotNull Text of(@NotNull String key, String... format) throws NullPointerException {
        return TextUtil.get(key, format);
    }
}
