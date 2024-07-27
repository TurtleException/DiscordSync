package de.turtleboi.spigot.dsync.core;

import de.turtleboi.spigot.dsync.api.ObeliskAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSyncCore extends JavaPlugin {
    @Override
    public void onEnable() {
        ObeliskAPI api = ObeliskAPI.getInstance();

        this.saveResource("config.yml", false);
    }
}
