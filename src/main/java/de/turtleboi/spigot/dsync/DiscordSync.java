package de.turtleboi.spigot.dsync;

import de.turtleboi.spigot.dsync.util.JDALogFilter;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSync extends JavaPlugin {
    public static DiscordSync plugin;

    public DiscordSync() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        this.saveResource("config.yml", false);
        this.saveResource("users.yml", false);

        JDALogFilter jdaLogFilter = new JDALogFilter(this);
        jdaLogFilter.start();
    }
}
