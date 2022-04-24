package de.eldritch.spigot.discord_sync.discord;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.util.Status;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class PresenceHandler {
    public static void update() {
        DiscordService discordService = DiscordSync.singleton.getDiscordService();

        if (discordService == null) return;


        final int players = DiscordSync.singleton.getServer().getOnlinePlayers().size();

        final OnlineStatus onlineStatus;
        if (DiscordSync.singleton.getStatus().equals(Status.STOPPING)) {
            onlineStatus = OnlineStatus.DO_NOT_DISTURB;
        } else if (DiscordSync.singleton.getStatus().equals(Status.STOPPED)) {
            onlineStatus = OnlineStatus.OFFLINE;
        } else {
            onlineStatus = players > 0
                    ? OnlineStatus.ONLINE
                    : OnlineStatus.IDLE;
        }

        final Activity activity;
        if (DiscordSync.singleton.getStatus().equals(Status.RUNNING)) {
            activity = players > 0
                    ? Activity.playing("Minecraft (%s)".formatted(players))
                    : Activity.playing("Minecraft");
        } else {
            activity = null;
        }


        discordService.getJDA().getPresence().setPresence(onlineStatus, activity);
    }
}
