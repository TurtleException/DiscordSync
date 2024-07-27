package de.turtleboi.spigot.dsync.embedded;

import de.turtleboi.spigot.dsync.api.ObeliskAPI;
import de.turtleboi.spigot.dsync.api.entity.dao.UserDAO;
import de.turtleboi.spigot.dsync.embedded.user.UserManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DiscordSyncEmbedded extends JavaPlugin implements ObeliskAPI {
    private UserManager userManager;

    @Override
    public void onEnable() {
        this.saveResource("users.yaml", false);

        YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "users.yaml"));
        this.userManager = new UserManager(this, userConfig);

        JDALogFilter jdaLogFilter = new JDALogFilter(this);
        jdaLogFilter.start();

        ObeliskAPI.register(this);
    }

    @Override
    public @NotNull UserDAO getUserDAO() {
        return this.userManager;
    }
}
