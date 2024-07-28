package de.turtleboi.spigot.dsync.core;

import de.turtleboi.spigot.dsync.api.DiscordSyncAPI;
import de.turtleboi.spigot.dsync.api.chat.MessageRouter;
import de.turtleboi.spigot.dsync.core.chat.MessageDispatcher;
import de.turtleboi.spigot.dsync.core.command.PluginCommand;
import de.turtleboi.spigot.dsync.core.listener.ChatInterceptor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class DiscordSyncCore extends JavaPlugin {
    private MessageDispatcher dispatcher;
    private TextComponent chatPrefix;

    @Override
    public void onEnable() {
        DiscordSyncAPI api;
        try {
            api = DiscordSyncAPI.getInstance();
        } catch (IllegalStateException e) {
            this.getLogger().log(Level.SEVERE, "It looks like no adapter is registered. DSync Core does not work as a standalone plugin!");
            throw e;
        }

        this.saveResource("config.yml", false);

        String chatPrefixStr = this.getConfig().getString("chatPrefix", "§8[§6DiscordSync§8]");
        TextComponent prefixComp1 = new TextComponent(TextComponent.fromLegacyText(chatPrefixStr));
        TextComponent prefixComp2 = new TextComponent(" ");
        this.chatPrefix = new TextComponent(prefixComp1, prefixComp2);
        this.chatPrefix.setColor(ChatColor.GRAY);

        // noinspection DataFlowIssue
        this.getCommand("dsync").setExecutor(new PluginCommand(this));

        this.dispatcher = new MessageDispatcher(this);
        MessageRouter router = api.getMessageRouter();
        router.registerHandler(this.dispatcher);

        this.getServer().getPluginManager().registerEvents(new ChatInterceptor(), this);
    }

    @Override
    public void onDisable() {
        try {
            DiscordSyncAPI api = DiscordSyncAPI.getInstance();
            MessageRouter router = api.getMessageRouter();
            router.unregisterHandler(this.dispatcher);
            this.dispatcher = null;
        } catch (IllegalStateException ignored) { }
    }

    public @NotNull TextComponent getChatPrefix() {
        return this.chatPrefix.duplicate();
    }
}
