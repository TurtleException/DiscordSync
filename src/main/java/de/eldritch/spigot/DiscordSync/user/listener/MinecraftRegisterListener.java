package de.eldritch.spigot.DiscordSync.user.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.user.User;
import de.eldritch.spigot.DiscordSync.user.UserAssociationService;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinecraftRegisterListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user = getUAService().get(user1 -> user1.getMinecraft().getUniqueId().equals(event.getPlayer().getUniqueId()));
        if (user == null) {
            DiscordSync.singleton.getLogger().info("Player '" + event.getPlayer().getName() + "' is not registered yet.");

            event.getPlayer().spigot().sendMessage(DiscordSync.getChatPrefix(),
                    new TextComponent(ChatColor.GRAY + "Nutze "),
                    new TextComponent(new ComponentBuilder("/verify <Discord-Name>")
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/verify "))
                            .color(ChatColor.AQUA).bold(true)
                            .create()),
                    new TextComponent(ChatColor.GRAY + " um dich auf Discord zu verifizieren.")
            );
        } else {
            DiscordSync.singleton.getLogger().info("Player '" + event.getPlayer().getName() + "' is already registered.");
        }
    }

    private UserAssociationService getUAService() {
        return DiscordSync.singleton.getUserAssociationService();
    }
}
