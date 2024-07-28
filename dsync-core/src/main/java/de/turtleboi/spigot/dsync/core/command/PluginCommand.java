package de.turtleboi.spigot.dsync.core.command;

import de.turtleboi.spigot.dsync.api.DiscordSyncAPI;
import de.turtleboi.spigot.dsync.api.chat.MessageHandler;
import de.turtleboi.spigot.dsync.api.chat.MessageHistory;
import de.turtleboi.spigot.dsync.api.chat.MessageRouter;
import de.turtleboi.spigot.dsync.core.DiscordSyncCore;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PluginCommand implements CommandExecutor {
    private final DiscordSyncCore plugin;

    public PluginCommand(@NotNull DiscordSyncCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender || sender.isOp())) {
            this.sendMessage(sender, "You are not permitted to use this command!");
            return true;
        }

        DiscordSyncAPI api;
        try {
            api = DiscordSyncAPI.getInstance();
        } catch (IllegalStateException e) {
            this.sendMessage(sender, "The API is §coffline", "!");
            return true;
        }
        this.sendMessage(sender, "The API is §aonline", ".");


        MessageRouter router = api.getMessageRouter();

        Set<MessageHandler> handlers = router.getHandlers();
        this.sendMessage(sender, "Message handlers§8: §e" + handlers.size());

        MessageHistory history = router.getHistory();
        this.sendMessage(sender, "Cached messages§8: §e" + history.size() + "§8/§e" + history.capacity());

        return true;
    }

    private void sendMessage(@NotNull CommandSender sender, @NotNull String... text) {
        TextComponent message = this.plugin.getChatPrefix();

        for (String s : text)
            message.addExtra(new TextComponent(TextComponent.fromLegacyText(s)));

        sender.spigot().sendMessage(message);
    }
}
