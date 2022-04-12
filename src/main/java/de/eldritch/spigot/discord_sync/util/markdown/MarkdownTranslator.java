package de.eldritch.spigot.discord_sync.util.markdown;

import java.util.regex.Pattern;

public class MarkdownTranslator {
    private static final String MARKDOWN_REGEX = "(?<format>(?>\\*\\*|__|~~|\\|\\||```|\\*|_|`))(?<content>(?>.)+?)\\k<format>";
    private static final Pattern MARKDOWN_PATTERN = Pattern.compile(MARKDOWN_REGEX);

    public static String toMarkdown(String legacyText) {
        return legacyText;
    }

    public static String toLegacyText(String markdown) {
        return markdown;
    }
}
