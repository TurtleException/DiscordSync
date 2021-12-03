package de.eldritch.spigot.DiscordSync.user.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.user.User;
import de.eldritch.spigot.DiscordSync.user.UserAssociationService;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

            ComponentBuilder builder = new ComponentBuilder();
            builder.append("[")
                    .color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
                    .append("SERVER")
                    .color(net.md_5.bungee.api.ChatColor.GREEN)
                    .append("] ")
                    .color(net.md_5.bungee.api.ChatColor.DARK_GRAY);

            TextComponent component = new TextComponent(builder.create());
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(ChatColor.GREEN + "DiscordSync\n" + ChatColor.ITALIC.toString() + ChatColor.GRAY.toString() + "v" + DiscordSync.singleton.getDescription().getVersion())
            ));

            event.getPlayer().spigot().sendMessage(component, new TextComponent(ChatColor.GRAY + "Dein Account ist nicht mit Discord verbunden, melde dich bitte bei der Serverleitung."));
        } else {
            DiscordSync.singleton.getLogger().info("Player '" + event.getPlayer().getName() + "' is already registered.");
        }
    }

    private UserAssociationService getUAService() {
        return DiscordSync.singleton.getUserAssociationService();
    }
}
