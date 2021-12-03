package de.eldritch.spigot.DiscordSync.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

public class DiscordUtil {
    public static final int COLOR_NEUTRAL = 0x2F3136;

    public static final String FOOTER_TEXT = "TurtleBot | DiscordSync";
    public static final String FOOTER_URL  = "https://github.com/TurtleException/DiscordSync/blob/main/icon_128.png?raw=true";

    public static TemporalAccessor getTimestamp() {
        return Instant.now().atOffset(ZoneOffset.UTC);
    }
}
