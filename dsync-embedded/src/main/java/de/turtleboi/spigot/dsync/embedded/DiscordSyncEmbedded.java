package de.turtleboi.spigot.dsync.embedded;

import de.turtleboi.spigot.dsync.api.ObeliskAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSyncEmbedded extends JavaPlugin implements ObeliskAPI {
    @Override
    public void onEnable() {
        this.saveResource("users.yml", false);

        JDALogFilter jdaLogFilter = new JDALogFilter(this);
        jdaLogFilter.start();

        ObeliskAPI.register(this);
    }
}
