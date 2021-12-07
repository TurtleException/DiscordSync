package de.eldritch.spigot.DiscordSync.user.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.Container;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import de.eldritch.spigot.DiscordSync.user.User;
import de.eldritch.spigot.DiscordSync.user.UserAssociationService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinecraftRegisterListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user = getUAService().get(user1 -> user1.getMinecraft().getUniqueId().equals(event.getPlayer().getUniqueId()));
        if (user == null) {
            DiscordSync.singleton.getLogger().info("Player '" + event.getPlayer().getName() + "' is not registered yet.");

            Bukkit.getScheduler().runTaskLaterAsynchronously(DiscordSync.singleton, () ->
                    MessageService.sendMessage(event.getPlayer(),
                            "user.verify.usage",
                            "user.verify.example"
                    ), 40L);
        } else {
            DiscordSync.singleton.getLogger().info("Player '" + event.getPlayer().getName() + "' is already registered.");
        }
    }

    private UserAssociationService getUAService() {
        return DiscordSync.singleton.getUserAssociationService();
    }
}
