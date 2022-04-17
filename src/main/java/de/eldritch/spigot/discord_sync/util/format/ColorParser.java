package de.eldritch.spigot.discord_sync.util.format;

import org.bukkit.ChatColor;

public class ColorParser {
    public static String format(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
