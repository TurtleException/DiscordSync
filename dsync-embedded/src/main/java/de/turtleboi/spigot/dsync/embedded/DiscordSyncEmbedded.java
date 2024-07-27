package de.turtleboi.spigot.dsync.embedded;

import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSyncEmbedded extends JavaPlugin {
    @Override
    public void onEnable() {
        this.saveResource("users.yml", false);

        JDALogFilter jdaLogFilter = new JDALogFilter(this);
        jdaLogFilter.start();
    }
}
