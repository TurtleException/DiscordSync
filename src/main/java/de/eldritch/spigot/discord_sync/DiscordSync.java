package de.eldritch.spigot.discord_sync;

import de.eldritch.spigot.discord_sync.discord.DiscordService;
import de.eldritch.spigot.discord_sync.user.AvatarHandler;
import de.eldritch.spigot.discord_sync.sync.SynchronizationService;
import de.eldritch.spigot.discord_sync.sync.listener.MinecraftChatListener;
import de.eldritch.spigot.discord_sync.sync.listener.MinecraftEventListener;
import de.eldritch.spigot.discord_sync.sync.listener.MinecraftJoinListener;
import de.eldritch.spigot.discord_sync.text.TextUtil;
import de.eldritch.spigot.discord_sync.user.UserService;
import de.eldritch.spigot.discord_sync.util.ConfigUtil;
import de.eldritch.spigot.discord_sync.util.version.Version;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * The plugin main class.
 */
@SuppressWarnings("unused")
public class DiscordSync extends JavaPlugin {
    public static DiscordSync singleton;

    private Version version;

    private UserService            userService;
    private SynchronizationService synchronizationService;
    private DiscordService         discordService;
    private AvatarHandler          avatarHandler;

    @Override
    public void onEnable() {
        singleton = this;

        try {
            this.prepare();
            this.checks();
            this.init();
        } catch (Exception e) {
            if (e instanceof RuntimeException exc) {
                throw exc;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Initialization steps before checking (e.g. for completeness of data).
     * @see DiscordSync#checks()
     * @see DiscordSync#init()
     */
    private void prepare() throws Exception {
        // retrieve version from description and parse it
        this.version = Version.parse(this.getDescription().getVersion());

        // apply defaults manually because bukkit will only do that if the file does not exist
        ConfigUtil.applyDefaults(getConfig(), "config.yml");

        TextUtil.init();
    }

    /**
     * Checks if the plugin can run in its current state.
     * @see DiscordSync#prepare()
     * @see DiscordSync#init()
     */
    private void checks() throws Exception {
        ConfigUtil.validatePluginConfig();
    }

    /**
     * Final initialization that mostly relies on other initialization succeeding.
     * @see DiscordSync#prepare()
     * @see DiscordSync#checks()
     */
    private void init() throws Exception {
        getLogger().log(Level.FINE, "Initializing UserService.");
        userService = new UserService();

        getLogger().log(Level.FINE, "Initializing SynchronizationService.");
        synchronizationService = new SynchronizationService();

        getLogger().log(Level.FINE, "Initializing DiscordService.");
        discordService = new DiscordService();

        getLogger().log(Level.FINE, "Initializing EmoteHandler.");
        avatarHandler = new AvatarHandler();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MinecraftChatListener(), this);
        getServer().getPluginManager().registerEvents(new MinecraftEventListener(), this);
        getServer().getPluginManager().registerEvents(new MinecraftJoinListener(), this);
    }

    /* ----- ----- ----- */

    @Override
    public void onDisable() {
        discordService.shutdown();
        discordService = null;
    }

    /* ----- ----- ----- */

    public Version getVersion() {
        return version;
    }

    public UserService getUserService() {
        return userService;
    }

    public SynchronizationService getSynchronizationService() {
        return synchronizationService; }

    public DiscordService getDiscordService() {
        return discordService;
    }

    public AvatarHandler getAvatarHandler() {
        return avatarHandler;
    }
}
