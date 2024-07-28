package de.turtleboi.spigot.dsync.core;

import de.turtleboi.spigot.dsync.api.DiscordSyncAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSyncCore extends JavaPlugin {
    @Override
    public void onEnable() {
        DiscordSyncAPI api = DiscordSyncAPI.getInstance();

        this.saveResource("config.yml", false);
    }
}
