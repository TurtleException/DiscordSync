package de.turtleboi.spigot.dsync.listeners;

import de.turtleboi.spigot.dsync.DiscordSync;
import de.turtleboi.spigot.dsync.SyncUser;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class UserListener extends ListenerAdapter implements Listener {
    private final DiscordSync plugin;

    public UserListener(@NotNull DiscordSync plugin) {
        this.plugin = plugin;
    }

    /* - DISCORD - */

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        super.onGuildMemberRemove(event);
    }

    /* - MINECRAFT - */

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        SyncUser user = plugin.getUser(event.getPlayer().getUniqueId());

        // ignore if this user is already registered
        if (user != null) return;

        // register new user
        plugin.putUser(event.getPlayer().getUniqueId(), event.getPlayer().getDisplayName());
    }

    // TODO: player ban (spigot doesn't support that :/)
}
