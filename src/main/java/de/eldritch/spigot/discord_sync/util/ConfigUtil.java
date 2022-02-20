package de.eldritch.spigot.discord_sync.util;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.DiscordUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class ConfigUtil {
    /**
     * Retrieves the configuration at the specified path and applies it as defaults to the {@link FileConfiguration}
     * provided as the first parameter.
     * @param config {@link FileConfiguration} to apply defaults to.
     * @param resource Resource path to defaults config.
     * @throws IOException if the defaults-config fails to load.
     * @throws InvalidConfigurationException if the defaults-config fails to load.
     */
    public static void applyDefaults(@NotNull FileConfiguration config, @NotNull String resource) throws IOException, InvalidConfigurationException {
        YamlConfiguration defaults = new YamlConfiguration();
        InputStream resourceStream = DiscordSync.singleton.getResource(resource);

        if (resourceStream == null)
            throw new NullPointerException("Resource InputStream turned out to be null");

        defaults.load(new InputStreamReader(resourceStream));
        config.setDefaults(defaults);
    }

    /**
     * Validates the plugin {@link FileConfiguration} provided by Bukkit via {@link JavaPlugin#getConfig()} and checks
     * some values manually.
     * @throws InvalidConfigurationException if the plugin configuration is invalid. More information will be provided
     *                                       in the exception message.
     * @see ConfigUtil#validatePresent(FileConfiguration, String, String...)
     * @see ConfigUtil#validateNotNull(FileConfiguration, String, String...)
     */
    public static void validatePluginConfig() throws InvalidConfigurationException {
        FileConfiguration config = DiscordSync.singleton.getConfig();

        validateVersion(config);

        /* ----- MANUAL CHECKS ----- */
        validateNotNull(config, "discord.token", DiscordUtil.LOG_INVALID_TOKEN);
    }

    private static void validateVersion(@NotNull FileConfiguration config) throws InvalidConfigurationException {
        int version = config.getInt("configVersion", -1);
        int required;

        // retrieve required version
        try {
            YamlConfiguration defaultConfig = new YamlConfiguration();
            InputStream resourceStream = DiscordSync.singleton.getResource("config.yml");

            if (resourceStream == null)
                throw new NullPointerException("Resource InputStream turned out to be null");

            defaultConfig.load(new InputStreamReader(resourceStream));
            required = defaultConfig.getInt("configVersion", -1);
        } catch (Exception e) {
            throw new InvalidConfigurationException("Unable to validate config version", e);
        }

        // compare versions
        if (required == -1)
            throw new InvalidConfigurationException("Unable to validate config version");
        if (version != required)
            throw new InvalidConfigurationException("Config version does not match required version");
    }

    private static void validatePresent(@NotNull FileConfiguration config, @NotNull String key, String... logNotes) throws InvalidConfigurationException {
        if (!config.contains(key)) {
            log(logNotes);
            throw new InvalidConfigurationException("Missing " + key);
        }
    }

    private static void validateNotNull(@NotNull FileConfiguration config, @NotNull String key, String... logNotes) throws InvalidConfigurationException {
        validatePresent(config, key, logNotes);
        if (config.get(key) == null) {
            log(logNotes);
            throw new InvalidConfigurationException("Illegal null value at " + key);
        }
    }

    private static void log(String... logNotes) {
        for (String logNote : logNotes) {
            DiscordSync.singleton.getLogger().log(Level.INFO, logNote);
        }
    }
}
