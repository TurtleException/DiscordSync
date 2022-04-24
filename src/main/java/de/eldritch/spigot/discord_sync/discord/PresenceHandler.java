package de.eldritch.spigot.discord_sync.discord;

import de.eldritch.spigot.discord_sync.DiscordSync;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class PresenceHandler {
    public static void update() {
        DiscordService discordService = DiscordSync.singleton.getDiscordService();

        if (discordService == null) return;


        final int players = DiscordSync.singleton.getServer().getOnlinePlayers().size();

        final OnlineStatus onlineStatus = players > 0
                ? OnlineStatus.ONLINE
                : OnlineStatus.IDLE;
        final Activity     activity     = players > 0
                ? Activity.playing("Minecraft (%s)".formatted(players))
                : Activity.playing("Minecraft");

        discordService.getJDA().getPresence().setPresence(onlineStatus, activity);
    }

    public static void updateShutdown() {
        DiscordService discordService = DiscordSync.singleton.getDiscordService();

        if (discordService == null) return;


        final OnlineStatus onlineStatus = OnlineStatus.DO_NOT_DISTURB;
        final Activity     activity     = Activity.playing("Minecraft");

        discordService.getJDA().getPresence().setPresence(onlineStatus, activity);
    }
}
