package de.eldritch.spigot.discord_sync;

import de.eldritch.spigot.discord_sync.util.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The plugin main class.
 */
@SuppressWarnings("unused")
public class DiscordSync extends JavaPlugin {
    public static DiscordSync singleton;

    @Override
    public void onEnable() {
        singleton = this;

        try {
            this.prepare();
            this.checks();
            this.init();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private void prepare() throws Exception {
        // apply defaults manually because bukkit will only do that if the file does not exist
        ConfigUtil.applyDefaults(getConfig(), "config.yml");
    }

    private void checks() throws Exception {
        ConfigUtil.validatePluginConfig();
    }

    private void init() throws Exception {

    }
}
