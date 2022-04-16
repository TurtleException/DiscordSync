package de.eldritch.spigot.discord_sync;

import de.eldritch.spigot.discord_sync.discord.DiscordService;
import de.eldritch.spigot.discord_sync.discord.PresenceHandler;
import de.eldritch.spigot.discord_sync.discord.PresenceRelevantListener;
import de.eldritch.spigot.discord_sync.sync.SynchronizationService;
import de.eldritch.spigot.discord_sync.sync.listener.MinecraftChatListener;
import de.eldritch.spigot.discord_sync.sync.listener.MinecraftEventListener;
import de.eldritch.spigot.discord_sync.sync.listener.MinecraftJoinListener;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.text.TextUtil;
import de.eldritch.spigot.discord_sync.user.AvatarHandler;
import de.eldritch.spigot.discord_sync.user.UserService;
import de.eldritch.spigot.discord_sync.user.verification.VerificationUtil;
import de.eldritch.spigot.discord_sync.util.ConfigUtil;
import de.eldritch.spigot.discord_sync.util.version.Version;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * The plugin main class.
 */
@SuppressWarnings("unused")
public class DiscordSync extends JavaPlugin {
    public static DiscordSync singleton;

    private static TextComponent chatPrefix;

    private Version version;

    private UserService            userService;
    private SynchronizationService synchronizationService;
    private DiscordService         discordService;
    private AvatarHandler          avatarHandler;

    @Override
    public void onEnable() {
        singleton = this;

        // TODO: remove (debug)
        getServer().getLogger().setLevel(Level.ALL);
        getLogger().setLevel(Level.ALL);

        try {
            this.prepare();
            this.checks();
            this.init();

            this.registerListeners();
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
        ConfigUtil.saveConfig(getConfig(), "config");

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
        getLogger().log(Level.FINE, "Initializing DiscordService.");
        discordService = new DiscordService();

        getLogger().log(Level.FINE, "Initializing UserService.");
        userService = new UserService();

        getLogger().log(Level.FINE, "Initializing SynchronizationService.");
        synchronizationService = new SynchronizationService();

        getLogger().log(Level.FINE, "Initializing EmoteHandler.");
        avatarHandler = new AvatarHandler();

        getLogger().log(Level.FINE, "Initializing verification.");
        VerificationUtil.initCommands();
        VerificationUtil.initListener();

        PresenceHandler.update();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MinecraftChatListener(), this);
        getServer().getPluginManager().registerEvents(new MinecraftEventListener(), this);
        getServer().getPluginManager().registerEvents(new MinecraftJoinListener(), this);

        getServer().getPluginManager().registerEvents(new PresenceRelevantListener(), this);
    }

    /* ----- ----- ----- */

    @Override
    public void onDisable() {
        try {
            this.shutdown();
        } catch (Exception e) {
            if (e instanceof RuntimeException exc) {
                throw exc;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private void shutdown() throws Exception {
        // save to prevent data loss in case shutdown fails
        ConfigUtil.saveConfig(getConfig(), "config");

        getLogger().log(Level.FINE, "Shutting down UserService.");
        userService.saveUsers();
        userService.saveConfig();

        getLogger().log(Level.FINE, "Saving config.");
        saveConfig();

        discordService.shutdown();
        discordService = null;

        synchronizationService = null;
        userService            = null;
        avatarHandler          = null;

        // save again for possible changes while shutting down
        ConfigUtil.saveConfig(getConfig(), "config");
    }

    /* ----- ----- ----- */

    public static TextComponent getChatPrefix() {
        if (chatPrefix == null) {
            chatPrefix = Text.of("general.prefix").toBaseComponent();

            chatPrefix.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new net.md_5.bungee.api.chat.hover.content.Text(Text.of(
                            "general.prefix.verbose",
                            singleton.getVersion().toString()
                    ).content())
            ));
        }

        return chatPrefix;
    }

    /* ----- ----- ----- */

    public Version getVersion() {
        return version;
    }

    public UserService getUserService() {
        return userService;
    }

    public SynchronizationService getSynchronizationService() {
        return synchronizationService;
    }

    public DiscordService getDiscordService() {
        return discordService;
    }

    public AvatarHandler getAvatarHandler() {
        return avatarHandler;
    }
}
