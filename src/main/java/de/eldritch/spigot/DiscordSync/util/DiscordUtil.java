package de.eldritch.spigot.DiscordSync.util;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

public class DiscordUtil {
    public static final int COLOR_NEUTRAL = 0x2F3136;

    public static final String FOOTER_TEXT = "TurtleBot | DiscordSync";

    public static TemporalAccessor getTimestamp() {
        return Instant.now().atOffset(ZoneOffset.UTC);
    }

    public static @Nullable String getAvatarURL() {
        if (DiscordSync.singleton.getDiscordAPI() != null
                && DiscordSync.singleton.getDiscordAPI().getJDA() != null) {
            return DiscordSync.singleton.getDiscordAPI().getJDA().getSelfUser().getAvatarUrl();
        } else {
            return null;
        }
    }
}
