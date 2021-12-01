package de.eldritch.spigot.DiscordSync.module;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Represents a specific section of the plugin, usually to allow
 * enabling / disabling them individually.
 */
public abstract class PluginModule {
    private final String moduleName = this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().length() - "Module".length()).toLowerCase();
    private final ConfigurationSection config = DiscordSync.singleton.getConfig().getConfigurationSection("module." + moduleName);

    private boolean enabled;

    public PluginModule() throws PluginModuleEnableException {

    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void setEnabled(boolean b) {
        if (enabled && !b) {
            enabled = false;
            this.onDisable();
        } else if (!enabled && b) {
            enabled = true;
            this.onEnable();
        }
    }

    /**
     * Provides the {@link ConfigurationSection} of the module, which is stored in
     * the plugins main {@link FileConfiguration}.
     */
    public ConfigurationSection getConfig() {
        return config;
    }

    /**
     * Provides the module's simple name.
     * <p>
     *     The name of "ExampleModule.java" would be "example".
     * </p>
     */
    public String getName() {
        return moduleName;
    }
}