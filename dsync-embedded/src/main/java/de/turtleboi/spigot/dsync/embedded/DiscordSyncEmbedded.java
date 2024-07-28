package de.turtleboi.spigot.dsync.embedded;

import de.turtleboi.spigot.dsync.api.DiscordSyncAPI;
import de.turtleboi.spigot.dsync.api.chat.MessageRouter;
import de.turtleboi.spigot.dsync.api.entity.dao.UserDAO;
import de.turtleboi.spigot.dsync.embedded.user.UserManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DiscordSyncEmbedded extends JavaPlugin implements DiscordSyncAPI {
    private UserManager userManager;
    private MessageRouter messageRouter;

    @Override
    public void onEnable() {
        this.saveResource("config.yml", false);
        this.saveResource("users.yaml", false);

        YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "users.yaml"));
        this.userManager = new UserManager(this, userConfig);

        int backlog = this.getConfig().getInt("messageBacklog", 1000);
        this.messageRouter = new MessageRouter(backlog);

        JDALogFilter jdaLogFilter = new JDALogFilter(this);
        jdaLogFilter.start();

        DiscordSyncAPI.register(this);
    }

    @Override
    public @NotNull MessageRouter getMessageRouter() {
        return this.messageRouter;
    }

    @Override
    public @NotNull UserDAO getUserDAO() {
        return this.userManager;
    }
}
