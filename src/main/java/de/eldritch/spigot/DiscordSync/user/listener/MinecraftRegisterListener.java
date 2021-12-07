package de.eldritch.spigot.DiscordSync.user.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import de.eldritch.spigot.DiscordSync.user.User;
import de.eldritch.spigot.DiscordSync.user.UserAssociationService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinecraftRegisterListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user = getUAService().get(user1 -> user1.getMinecraft().getUniqueId().equals(event.getPlayer().getUniqueId()));
        if (user == null) {
            DiscordSync.singleton.getLogger().info("Player '" + event.getPlayer().getName() + "' is not registered yet.");

            event.getPlayer().spigot().sendMessage(
                    MessageService.get("general.prefix"),
                    MessageService.get("user.verify.usage"),
                    MessageService.get("user.verify.example")
            );
        } else {
            DiscordSync.singleton.getLogger().info("Player '" + event.getPlayer().getName() + "' is already registered.");
        }
    }

    private UserAssociationService getUAService() {
        return DiscordSync.singleton.getUserAssociationService();
    }
}
