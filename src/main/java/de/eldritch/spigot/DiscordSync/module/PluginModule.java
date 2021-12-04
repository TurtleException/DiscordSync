package de.eldritch.spigot.DiscordSync.module;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Dictionary;
import java.util.logging.*;

/**
 * Represents a specific section of the plugin, usually to allow
 * enabling / disabling them individually.
 */
public abstract class PluginModule {
    private final String moduleName = this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().length() - "Module".length()).toLowerCase();
    private ConfigurationSection config = DiscordSync.singleton.getConfig().getConfigurationSection("module." + moduleName);

    private final Logger logger;

    private boolean enabled;

    public PluginModule() {
        logger = Logger.getLogger("MODULE | " + moduleName.toUpperCase());
        logger.addHandler(new StreamHandler() {
            @Override
            public void publish(LogRecord record) {
                record.setMessage("[" + record.getLoggerName() + "] " + record.getMessage());
                DiscordSync.singleton.getLogger().log(record);
            }
        });
    }

    public void onEnable() throws PluginModuleEnableException {

    }

    public void onDisable() {

    }

    public void setEnabled(boolean b) {
        if (enabled && !b) {
            enabled = false;
            this.onDisable();
        } else if (!enabled && b) {
            try {
                enabled = true;
                this.onEnable();
            } catch (PluginModuleEnableException e) {
                DiscordSync.singleton.getLogger().log(Level.WARNING, "Exception while attempting to enable module '" + getName() + "'.", e);
                enabled = false;
            }
        }
    }

    /**
     * Provides the {@link ConfigurationSection} of the module, which is stored in
     * the plugins main {@link FileConfiguration}.
     */
    public ConfigurationSection getConfig() {
        if (config == null) {
            config = DiscordSync.singleton.getConfig().createSection("module." + moduleName);
        }
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

    public Logger getLogger() {
        return logger;
    }
}