package de.turtleboi.spigot.dsync.core;

import de.turtleboi.spigot.dsync.api.DiscordSyncAPI;
import de.turtleboi.spigot.dsync.api.chat.MessageRouter;
import de.turtleboi.spigot.dsync.core.chat.MessageDispatcher;
import de.turtleboi.spigot.dsync.core.listener.ChatInterceptor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class DiscordSyncCore extends JavaPlugin {
    private MessageDispatcher dispatcher;

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
}
