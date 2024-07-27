package de.turtleboi.spigot.dsync.core;

import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSync extends JavaPlugin {
    @Override
    public void onEnable() {
        this.saveResource("config.yml", false);
    }
}
