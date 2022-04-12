package de.eldritch.spigot.discord_sync.text;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public record Text(@NotNull String content) {
    public TextComponent toBaseComponent() {
        return new TextComponent(TextComponent.fromLegacyText(content));
    }

    @Override
    public String toString() {
        return content;
    }

    /* ------------------------- */

    /**
     * Provides a {@link Text} with its {@link String} content taken from a language file.
     * @param key Key to the String stored in a language file.
     * @param format Arguments to use as replacement for the format specified in the String.
     * @return Formatted {@link Text} from the specified language and its key.
     * @throws NullPointerException if the key does not point to a valid String.
     */
    public static @NotNull Text of(@NotNull String key, String... format) throws NullPointerException {
        return TextUtil.getFromPlugin(key, format);
    }

    public static @NotNull Text ofGame(@NotNull String key, String... format) throws NullPointerException {
        return TextUtil.getFromGame(key, format);
    }
}
