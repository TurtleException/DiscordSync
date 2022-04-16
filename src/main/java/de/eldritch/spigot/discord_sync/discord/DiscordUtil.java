package de.eldritch.spigot.discord_sync.discord;

import de.eldritch.spigot.discord_sync.DiscordSync;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class DiscordUtil {
    public static final String LOG_INVALID_TOKEN = "It looks like your token is missing or invalid. Please provide your"
            + " bot token under 'discord.token' in the plugins config. If you have trouble finding your token or"
            + " setting up a bot please refer to the wiki: https://github.com/TurtleException/DiscordSync/wiki";

    public static final String LOG_ACCESSOR_EXCEPTION = "Unexpected exception while initializing Accessor. Please check"
            + " if you provided valid snowflake IDs for your guild and for all listed channels.";

    /**
     * Default embed footer text.
     */
    public static final String FOOTER_TEXT = "DiscordSync v" + DiscordSync.singleton.getVersion().toString();

    /**
     * Neutral color equal to the embed background itself.
     */
    public static final int COLOR_NEUTRAL = 0x2F3136;

    public static final MessageEmbed DEFAULT_EMBED = new EmbedBuilder()
            .setFooter(FOOTER_TEXT, getAvatarURL())
            .setTimestamp(Instant.now())
            .setColor(COLOR_NEUTRAL)
            .build();

    public static long parseSnowflake(String input) throws NullPointerException, NumberFormatException {
        if (input == null)
            throw new NullPointerException("Input may not be null.");
        if (input.equals(""))
            throw new NullPointerException("Input may not be empty String.");

        long snowflake = Long.parseLong(input);

        if (snowflake < 0)
            throw new NumberFormatException("Snowflake may not be negative number.");

        return snowflake;
    }

    /**
     * Provide the current avatar URL of the {@link net.dv8tion.jda.api.entities.SelfUser SelfUser}.
     */
    public static @Nullable String getAvatarURL() {
        return DiscordSync.singleton.getDiscordService().getJDA().getSelfUser().getAvatarUrl();
    }
}
