package de.eldritch.spigot.discord_sync.util.format;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.regex.Pattern;

/**
 * Util to handle markdown / legacy text translation.
 */
class MarkdownParser {
    /**
     * Regex to parse markdown into groups.
     */
    private static final String MARKDOWN_REGEX = "(?<format>(?>\\*\\*|__|~~|\\|\\||```|\\*|_|`))(?<content>(?>.)+?)\\k<format>";
    /**
     * Pattern of {@link MarkdownParser#MARKDOWN_REGEX}.
     */
    private static final Pattern MARKDOWN_PATTERN = Pattern.compile(MARKDOWN_REGEX);

    // TODO
    public static String toMarkdown(BaseComponent... legacyText) {
        return legacyText;
    }

    public static BaseComponent[] toComponent(String rawText) {

    }

    // TODO
    public static TextComponent toLegacyText(String markdown) {
        return markdown;
    }
}
