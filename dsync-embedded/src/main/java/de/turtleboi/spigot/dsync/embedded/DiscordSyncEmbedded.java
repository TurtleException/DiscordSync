package de.turtleboi.spigot.dsync.embedded;

import de.turtleboi.spigot.dsync.core.user.UserRegistry;
import de.turtleboi.spigot.dsync.user.UserRegistry;
import de.turtleboi.spigot.dsync.util.JDALogFilter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DiscordSyncEmbedded extends JavaPlugin {
    private UserRegistry userRegistry;

    @Override
    public void onEnable() {
        this.saveResource("users.yml", false);

        this.userRegistry = new UserRegistry();

        JDALogFilter jdaLogFilter = new JDALogFilter(this);
        jdaLogFilter.start();
    }

    public @NotNull UserRegistry getUserRegistry() {
        return this.userRegistry;
    }
}
