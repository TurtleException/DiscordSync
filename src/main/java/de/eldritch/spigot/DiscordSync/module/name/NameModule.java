package de.eldritch.spigot.DiscordSync.module.name;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;
import de.eldritch.spigot.DiscordSync.module.name.listener.DiscordNameListener;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class NameModule extends PluginModule {
    private final HashSet<UserWrapper> userWrappers = new HashSet<>();

    private final YamlConfiguration config = new YamlConfiguration();
    private final File configFile;

    public NameModule() throws PluginModuleEnableException {
        super();

        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new PluginModuleEnableException("Module is dependant on JDA connection.");

        File[] files = DiscordSync.singleton.getDataFolder().listFiles((dir, name) -> name.equals("users.yml"));
        if (files == null || files.length == 0) {
            DiscordSync.singleton.getLogger().info("users.yml does not exist. Attempting to create a new file...");
            try {
                if (!DiscordSync.singleton.getDataFolder().mkdir())
                    throw new IOException("Could not create plugin data folder.");
                new File(DiscordSync.singleton.getDataFolder(), "users.yml").createNewFile();

                DiscordSync.singleton.getLogger().info("users.yml created!");
            } catch (IOException e) {
                throw new PluginModuleEnableException("Unable to access users.yml.", e);
            }
        }

        try {
            configFile = Objects.requireNonNull(files)[0];
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new PluginModuleEnableException("Unable to load users.yml", e);
        }
    }

    @Override
    public void onEnable() {
        this.reloadUsers();

        DiscordSync.singleton.getDiscordAPI().getJDA().addEventListener(new DiscordNameListener(this));
    }

    public void reloadUsers() {
        userWrappers.clear();

        for (String key : getConfig().getKeys(false)) {
            try {
                userWrappers.add(new UserWrapper(UUID.fromString(key), getConfig().getLong(key)));
            } catch (Exception e) {
                DiscordSync.singleton.getLogger().warning("Unable to parse {" + key + ": " + getConfig().get(key, "null") + "} to user association.");
            }
        }
    }

    public void set(long discord, String name) {
        for (UserWrapper userWrapper : userWrappers) {
            if (userWrapper.getDiscord() == discord) {
                userWrapper.setName(name);
                return;
            }
        }
    }

    @Override
    public ConfigurationSection getConfig() {
        return config;
    }

    /**
     * Reloads the <code>users.yml</code> file and updates all userWrappers.
     */
    public void reload() {
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to reload users.yml.", e);
        }

        this.reloadUsers();
    }
}
