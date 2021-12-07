package de.eldritch.spigot.DiscordSync.module.chat.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import de.eldritch.spigot.DiscordSync.module.chat.ChatModule;
import de.eldritch.spigot.DiscordSync.module.chat.SynchronizedMinecraftMessage;
import de.eldritch.spigot.DiscordSync.user.User;
import de.eldritch.spigot.DiscordSync.util.DiscordUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MinecraftListener implements Listener {
    private ChatModule module;

    public MinecraftListener(ChatModule module) {
        this.module = module;
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        SynchronizedMinecraftMessage minecraftMessage = new SynchronizedMinecraftMessage(event.getMessage(), event.getPlayer());
        module.process(minecraftMessage);

        if (event.isCancelled())
            return;

        event.setCancelled(true);

        User user = DiscordSync.singleton.getUserAssociationService().get(user1 -> user1.getMinecraft().getUniqueId().equals(event.getPlayer().getUniqueId()));
        if (user != null) {
            DiscordSync.singleton.getServer().spigot().broadcast(
                    MessageService.get(
                        "module.chat.message.minecraft.registered",
                        user.getName(),
                        user.getName(),
                        user.getMinecraft().getName(),
                        DiscordUtil.getActualName(user.getDiscord().getUser())
                    ), MessageService.get(
                            "module.chat.message",
                            event.getMessage()
                    )
            );
        } else {
            DiscordSync.singleton.getServer().spigot().broadcast(
                    MessageService.get(
                        "module.chat.message.minecraft.generic",
                        event.getPlayer().getDisplayName(),
                        event.getPlayer().getDisplayName()
                    ), MessageService.get(
                            "module.chat.message",
                            event.getMessage()
                    )
            );
        }
    }
}