package de.turtleboi.spigot.dsync.core;

import de.turtleboi.spigot.dsync.api.DiscordSyncAPI;
import de.turtleboi.spigot.dsync.core.listener.ChatInterceptor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class DiscordSyncCore extends JavaPlugin {
    @Override
    public void onEnable() {
        try {
            DiscordSyncAPI api = DiscordSyncAPI.getInstance();
        } catch (IllegalStateException e) {
            this.getLogger().log(Level.SEVERE, "It looks like no adapter is registered. DSync Core does not work as a standalone plugin!");
            throw e;
        }

        this.saveResource("config.yml", false);

        this.getServer().getPluginManager().registerEvents(new ChatInterceptor(), this);
    }
}
