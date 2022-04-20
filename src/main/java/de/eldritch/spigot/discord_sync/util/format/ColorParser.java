package de.eldritch.spigot.discord_sync.util.format;

import org.bukkit.ChatColor;

/**
 * Util to handle spigot color translation.
 */
public class ColorParser {
    /**
     * Replaces all alternate color codes (see lang files) with native color codes.
     * @param str The message to format.
     * @return THe formatted message.
     */
    public static String format(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
