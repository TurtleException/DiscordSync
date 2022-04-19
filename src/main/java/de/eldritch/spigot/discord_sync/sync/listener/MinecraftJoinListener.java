package de.eldritch.spigot.discord_sync.sync.listener;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Level;

public class MinecraftJoinListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event == null) return;

        // user will be created if it does not exist yet
        User user = DiscordSync.singleton.getUserService().getByUUID(event.getPlayer().getUniqueId());

        DiscordSync.singleton.getLogger().log(Level.INFO, "Turtle ID of player " + event.getPlayer().getName() + " is " + user.getID());

        DiscordSync.singleton.getAvatarHandler().loadEmote(event.getPlayer());
    }
}
