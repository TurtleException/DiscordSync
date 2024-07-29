package de.turtleboi.spigot.dsync.embedded;

import de.turtleboi.spigot.dsync.api.DiscordSyncAPI;
import de.turtleboi.spigot.dsync.api.chat.MessageRouter;
import de.turtleboi.spigot.dsync.api.entity.dao.UserDAO;
import de.turtleboi.spigot.dsync.embedded.listener.StatusListener;
import de.turtleboi.spigot.dsync.embedded.user.UserManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DiscordSyncEmbedded extends JavaPlugin implements DiscordSyncAPI {
    private UserManager userManager;
    private MessageRouter messageRouter;

    private StatusListener statusListener;

    private JDA jda;

    @Override
    public void onLoad() {
        this.saveResource("config.yml", false);
        this.saveResource("users.yml", false);

        this.statusListener = new StatusListener(this);

        String token = this.getConfig().getString("discord.token");
        this.jda = JDABuilder.create(token,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT)
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.VOICE_STATE,
                        CacheFlag.STICKER,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.ROLE_TAGS,
                        CacheFlag.FORUM_TAGS,
                        CacheFlag.ONLINE_STATUS,
                        CacheFlag.SCHEDULED_EVENTS
                )
                .addEventListeners(this.statusListener)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.customStatus("Starting server..."))
                .build();

        YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "users.yml"));
        this.userManager = new UserManager(this, userConfig);

        int backlog = this.getConfig().getInt("messageBacklog", 1000);
        this.messageRouter = new MessageRouter(backlog);

        JDALogFilter jdaLogFilter = new JDALogFilter(this);
        jdaLogFilter.start();

        this.getLogger().log(Level.INFO, "Registering API");
        DiscordSyncAPI.register(this);
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this.statusListener, this);

        try {
            this.jda.awaitStatus(JDA.Status.CONNECTED, JDA.Status.SHUTDOWN);
        } catch (InterruptedException e) {
            throw new IllegalStateException("JDA failed to load", e);
        }

        if (this.jda.getStatus().equals(JDA.Status.SHUTDOWN))
            throw new IllegalStateException("JDA failed to load");
    }

    @Override
    public void onDisable() {
        if (this.jda != null) {
            this.jda.shutdown();
            try {
                // noinspection ResultOfMethodCallIgnored
                this.jda.awaitShutdown(8, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                this.getLogger().log(Level.WARNING, "JDA did not shut down properly", e);
            }
        }
    }

    public JDA getJDA() {
        return this.jda;
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
