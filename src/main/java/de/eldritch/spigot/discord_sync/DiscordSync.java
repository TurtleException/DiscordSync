package de.eldritch.spigot.discord_sync;

import de.eldritch.spigot.discord_sync.util.ConfigUtil;
import de.eldritch.spigot.discord_sync.util.version.Version;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The plugin main class.
 */
@SuppressWarnings("unused")
public class DiscordSync extends JavaPlugin {
    public static DiscordSync singleton;

    private Version version;

    @Override
    public void onEnable() {
        singleton = this;

        try {
            this.prepare();
            this.checks();
            this.init();
        } catch (Exception exc) {
            if (exc instanceof RuntimeException) {
                throw (RuntimeException) exc;
            } else {
                throw new RuntimeException(exc);
            }
        }
    }

    private void prepare() throws Exception {
        // retrieve version from description and parse it
        this.version = Version.parse(this.getDescription().getVersion());

        // apply defaults manually because bukkit will only do that if the file does not exist
        ConfigUtil.applyDefaults(getConfig(), "config.yml");
    }

    private void checks() throws Exception {
        ConfigUtil.validatePluginConfig();
    }

    private void init() throws Exception {

    }
}
