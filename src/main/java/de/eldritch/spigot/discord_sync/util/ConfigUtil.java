package de.eldritch.spigot.discord_sync.util;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.DiscordUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.logging.Level;

/**
 * A collection of utilities for YAML configurations.
 */
public class ConfigUtil {
    /**
     * Provides a {@link YamlConfiguration} linked with a {@link File} at the provided location. If a resource path is
     * provided that resource will be loaded as default. If the config file does not already exist a new one will be
     * created automatically.
     * @param path Relative path to the configuration <b>not including</b> the <code>.yml</code> suffix. The file will
     *             be located in the plugins' data folder.
     * @param resource Path to the resource that should be used as default config, <code>null</code> if no defaults
     *                 should be applied.
     * @return The configuration loaded from the file, including default values.
     * @throws IOException if the file is not actually a file, a new file could not be created, the configuration fails
     *                     to load or an exception occurs while applying the default values.
     * @throws InvalidConfigurationException if the configuration fails to load or an exception occurs while applying
     *                                       the default values.
     */
    public static YamlConfiguration getConfig(@NotNull String path, @Nullable String resource) throws IOException, InvalidConfigurationException {
        File file = IOUtil.getFile(path + ".yml");

        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(file);

        if (resource != null)
            applyDefaults(configuration, resource);

        return configuration;
    }

    /**
     * Saves a config to the provided path.
     * @param config Config to save.
     * @param path Path of the file (excluding the <code>.yml</code> suffix).
     * @throws IOException if the file could not be handled or the config could not be saved.
     */
    public static void saveConfig(@NotNull FileConfiguration config, @NotNull String path) throws IOException {
        File file = IOUtil.getFile(path + ".yml");

        config.save(file);
    }

    /**
     * Retrieves the configuration at the specified path and applies it as defaults to the {@link FileConfiguration}
     * provided as the first parameter.
     * @param config {@link FileConfiguration} to apply defaults to.
     * @param resource Resource path to defaults config, including the <code>.yml</code> suffix.
     * @throws IOException if the defaults-config fails to load.
     * @throws InvalidConfigurationException if the defaults-config fails to load.
     */
    public static void applyDefaults(@NotNull FileConfiguration config, @NotNull String resource) throws IOException, InvalidConfigurationException {
        YamlConfiguration defaults = new YamlConfiguration();
        InputStream resourceStream = DiscordSync.singleton.getResource(resource);

        if (resourceStream == null)
            throw new NullPointerException("Resource InputStream turned out to be null");

        defaults.load(new InputStreamReader(resourceStream));

        // apply defaults manually (defaults are a bit weird)
        Set<String> keys = config.getKeys(true);
        for (String key : defaults.getKeys(true)) {
            if (keys.contains(key)) continue;
            config.set(key, defaults.get(key));
        }
    }

    /**
     * Validates the plugin {@link FileConfiguration} provided by Bukkit via {@link JavaPlugin#getConfig()} and checks
     * some values manually.
     * @throws InvalidConfigurationException if the plugin configuration is invalid. More information will be provided
     *                                       in the exception message.
     * @see ConfigUtil#validatePresent(MemorySection, String, String...)
     */
    public static void validatePluginConfig() throws InvalidConfigurationException {
        FileConfiguration config = DiscordSync.singleton.getConfig();

        validateVersion(config);

        /* ----- MANUAL CHECKS ----- */
        validatePresent(config, "discord.token", DiscordUtil.LOG_INVALID_TOKEN);
    }

    /**
     * Validates that the config version of the effectively loaded config matches that of the resource config to ensure
     * compatibility. If the version does not match an {@link InvalidConfigurationException} will be thrown.
     * @param config {@link FileConfiguration} to verify.
     * @throws InvalidConfigurationException if the version does not match its resource config counterpart or if an
     *                                       Exception occurs while attempting to validate the version.
     */
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
            throw new InvalidConfigurationException("Unable to validate config version.");
        if (version != required)
            throw new InvalidConfigurationException("Config version (%s) does not match required version (%s).".formatted(version, required));
    }

    /**
     * Validates that a provided key is present (i.e. holds a value that is not <code>null</code>) in a
     * {@link MemorySection}. If the key is not present an {@link InvalidConfigurationException} will be thrown.
     * @param config The MemorySection to check for the key.
     * @param key The key that should hold a non-null value.
     * @param logMessages Messages to log (with {@link Level#INFO}) in case the validation fails.
     * @throws InvalidConfigurationException if the config does not contain the provided key.
     */
    private static void validatePresent(@NotNull MemorySection config, @NotNull String key, String... logMessages) throws InvalidConfigurationException {
        if (!config.contains(key)) {
            log(logMessages);
            throw new InvalidConfigurationException("Missing " + key);
        }
    }

    private static void log(String... logMessages) {
        for (String logNote : logMessages) {
            DiscordSync.singleton.getLogger().log(Level.INFO, logNote);
        }
    }
}
