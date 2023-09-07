package de.turtleboi.spigot.dsync.util;

import org.jetbrains.annotations.NotNull;

public class StringUtil {
    private StringUtil() { }

    public static @NotNull String format(@NotNull String pattern, String... format) {
        if (format == null || format.length == 0) return pattern;
        String buffer = pattern;
        for (int i = 0; i < format.length; i++)
            buffer = buffer.replaceAll("\\{" + i + "}", format[i]);
        return buffer;
    }
}
