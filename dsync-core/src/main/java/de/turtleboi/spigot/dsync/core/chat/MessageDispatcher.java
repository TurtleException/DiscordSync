package de.turtleboi.spigot.dsync.core.chat;

import de.turtleboi.fancyformat.Format;
import de.turtleboi.fancyformat.FormatText;
import de.turtleboi.fancyformat.format.SpigotComponentFormat;
import de.turtleboi.spigot.dsync.api.chat.MessageHandler;
import de.turtleboi.spigot.dsync.api.entity.Message;
import de.turtleboi.spigot.dsync.core.DiscordSyncCore;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;

public class MessageDispatcher implements MessageHandler {
    private final DiscordSyncCore plugin;

    private final Format<BaseComponent> format = new SpigotComponentFormat();

    public MessageDispatcher(@NotNull DiscordSyncCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessage(@NotNull Message message) {
        FormatText content = message.getContent();
        BaseComponent component = content.toFormat(this.format);

        this.plugin.getServer().spigot().broadcast(component);
    }
}
