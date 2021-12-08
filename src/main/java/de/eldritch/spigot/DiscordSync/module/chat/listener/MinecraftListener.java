package de.eldritch.spigot.DiscordSync.module.chat.listener;

import de.eldritch.spigot.DiscordSync.message.MessageService;
import de.eldritch.spigot.DiscordSync.module.chat.ChatModule;
import de.eldritch.spigot.DiscordSync.module.chat.SynchronizedMinecraftMessage;
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

        event.setFormat(MessageService.get(
                "module.chat.message.bare.minecraft",
                event.getPlayer().getDisplayName(),
                event.getMessage()
        ).toLegacyText());
    }
}