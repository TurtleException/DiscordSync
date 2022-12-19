package de.turtle_exception.discordsync;

import de.turtle_exception.fancyformat.Format;
import org.jetbrains.annotations.NotNull;

public class FormatHandler {
    private final DiscordSync plugin;

    private final String formatMinMin;
    private final String formatDisMin;
    private final String formatMinDis;
    private final String formatDisDis;

    // MINOR PERFORMANCE IMPROVEMENTS
    // Minecraft -> Minecraft
    private final boolean minMinUser;
    private final boolean minMinPlayer;
    private final boolean minMinMessage;
    // Discord -> Minecraft
    private final boolean disMinUser;
    private final boolean disMinDiscord;
    private final boolean disMinMessage;
    // Minecraft -> Discord
    private final boolean minDisUser;
    private final boolean minDisPlayer;
    private final boolean minDisEmote;
    private final boolean minDisMessage;
    // Discord -> Discord
    private final boolean disDisUser;
    private final boolean disDisDiscord;
    private final boolean disDisMention;
    private final boolean disDisGuild;
    private final boolean disDisMessage;

    public FormatHandler(@NotNull DiscordSync plugin) {
        this.plugin = plugin;

        this.formatMinMin = plugin.getConfig().getString("format.minecraftChat.minecraft");
        this.formatDisMin = plugin.getConfig().getString("format.minecraftChat.discord");
        this.formatMinDis = plugin.getConfig().getString("format.discordChat.minecraft");
        this.formatDisDis = plugin.getConfig().getString("format.discordChat.discord");

        if (formatMinMin == null || formatDisMin == null || formatMinDis == null || formatDisDis == null)
            throw new NullPointerException("config.yml is missing one or more format settings!");

        this.minMinUser    = formatMinMin.contains("%user%");
        this.minMinPlayer  = formatMinMin.contains("%player%");
        this.minMinMessage = formatMinMin.contains("%message%");

        this.disMinUser    = formatDisMin.contains("%user%");
        this.disMinDiscord = formatDisMin.contains("%discord%");
        this.disMinMessage = formatDisMin.contains("%message%");

        this.minDisUser    = formatMinDis.contains("%user%");
        this.minDisPlayer  = formatMinDis.contains("%player%");
        this.minDisEmote   = formatMinDis.contains("%emote%");
        this.minDisMessage = formatMinDis.contains("%message%");

        this.disDisUser    = formatDisDis.contains("%user%");
        this.disDisDiscord = formatDisDis.contains("%discord%");
        this.disDisMention = formatMinDis.contains("%mention%");
        this.disDisGuild   = formatDisDis.contains("%guild%");
        this.disDisMessage = formatDisDis.contains("%message%");
    }

    /* - - - */

    public @NotNull String toMinecraft(@NotNull SyncMessage message) {
        String content = message.content().toString(Format.MINECRAFT_LEGACY);

        if (message.sourceInfo().isMinecraft()) {
            String format = this.formatMinMin;

            if (minMinUser)
                format = format.replaceAll("%user%", message.author().getName());
            if (minMinPlayer)
                format = format.replaceAll("%player", message.sourceInfo().getPlayer().getDisplayName());
            if (minMinMessage)
                format = format.replaceAll("%message%", content);

            return format;
        } else {
            String format = this.formatDisMin;

            if (disMinUser)
                format = format.replaceAll("%user%", message.author().getName());
            if (disMinDiscord)
                format = format.replaceAll("%discord%", message.sourceInfo().getEffectiveDiscordName());
            if (disMinMessage)
                format = format.replaceAll("%message%", content);

            return format;
        }
    }

    public @NotNull String toDiscord(@NotNull SyncMessage message, long target) {
        String content = message.content().toString(Format.DISCORD);

        // TODO: make emotes for discord guild logos for Discord -> Discord messages

        if (message.sourceInfo().isMinecraft()) {
            String format = this.formatMinDis;

            if (minDisUser)
                format = format.replaceAll("%user%", message.author().getName());
            if (minDisPlayer)
                format = format.replaceAll("%player%", message.sourceInfo().getPlayer().getDisplayName());
            if (minDisEmote)
                format = format.replaceAll("%emote%", getEmote(message, target));
            if (minDisMessage)
                format = format.replaceAll("%message%", content);

            return format;
        } else {
            String format = this.formatDisDis;

            if (disDisUser)
                format = format.replaceAll("%user%", message.author().getName());
            if (disDisDiscord)
                format = format.replaceAll("%discord%", message.sourceInfo().getEffectiveDiscordName());
            if (disDisMention)
                format = format.replaceAll("%mention%", message.sourceInfo().getUser().getAsMention());
            if (disDisGuild)
                format = format.replaceAll("%guild%", getEmote(message, target));
            if (disDisMessage)
                format = format.replaceAll("%message%", content);

            return format;
        }
    }

    private @NotNull String getEmote(@NotNull SyncMessage message, long target) {
        if (message.sourceInfo().isMinecraft())
            return plugin.getEmoteHandler().getEmote(message.sourceInfo().getPlayer(), target);
        else
            return plugin.getEmoteHandler().getEmote(message.sourceInfo().getChannel());
    }
}
