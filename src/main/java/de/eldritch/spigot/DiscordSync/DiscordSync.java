package de.eldritch.spigot.DiscordSync;

import de.eldritch.spigot.DiscordSync.discord.DiscordAPI;
import de.eldritch.spigot.DiscordSync.discord.DiscordConnectionException;
import de.eldritch.spigot.DiscordSync.module.ModuleManager;
import de.eldritch.spigot.DiscordSync.module.chat.ChatModule;
import de.eldritch.spigot.DiscordSync.module.emote.EmoteModule;
import de.eldritch.spigot.DiscordSync.module.language.LanguageModule;
import de.eldritch.spigot.DiscordSync.module.status.StatusModule;
import de.eldritch.spigot.DiscordSync.module.whitelist.WhitelistModule;
import de.eldritch.spigot.DiscordSync.user.UserAssociationService;
import de.eldritch.spigot.DiscordSync.util.IllegalVersionException;
import de.eldritch.spigot.DiscordSync.util.Performance;
import de.eldritch.spigot.DiscordSync.util.Version;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class DiscordSync extends JavaPlugin {
    public static DiscordSync singleton;
    private String serverName;

    private DiscordAPI             discordAPI;
    private UserAssociationService uaService;
    private ModuleManager          moduleManager;

    @Override
    public void onEnable() {
        singleton = this;

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Performance(), 100L, 1L);

        // update server name
        try {
            Properties serverProperties = new Properties();
            File propertiesFile = new File(getDataFolder().getParentFile().getParentFile(), "server.properties");
            serverProperties.load(new FileReader(propertiesFile));
            serverName = (String) serverProperties.getOrDefault("server-name", "null");
        } catch (IOException e) {
            getLogger().warning("Unable to read server.properties!");
            serverName = "null";
        }

        try {
            discordAPI = new DiscordAPI();
        } catch (DiscordConnectionException e) {
            getLogger().log(Level.SEVERE, "Unable to instantiate DiscordAPI.", e);
        }

        try {
            uaService = new UserAssociationService();
            uaService.reload();
        } catch (NullPointerException | IOException e) {
            getLogger().log(Level.SEVERE, "Unable to instantiate UserAssociationService.", e);
        }

        moduleManager = new ModuleManager(
                Map.entry(ChatModule.class, new Object[]{}),
                Map.entry(EmoteModule.class, new Object[]{}),
                Map.entry(LanguageModule.class, new Object[]{}),
                Map.entry(StatusModule.class, new Object[]{}),
                Map.entry(WhitelistModule.class, new Object[]{})
        );

        moduleManager.getRegisteredModules().forEach(pluginModule -> pluginModule.setEnabled(true));
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling modules...");
        moduleManager.getRegisteredModules().forEach(pluginModule -> {
            moduleManager.unregister(pluginModule);
            getLogger().info("Module '" + pluginModule.getName() + "' disabled.");
        });

        discordAPI.getJDA().shutdown();
    }


    public DiscordAPI getDiscordAPI() {
        return discordAPI;
    }

    public UserAssociationService getUserAssociationService() {
        return uaService;
    }

    /**
     * @return The <code>server-name</code> specified in <code>server.properties</code>.
     */
    public String getServerName() {
        return serverName;
    }

    public Version getVersion() {
        try {
            return Version.parse(this.getDescription().getVersion());
        } catch (IllegalVersionException e) {
            getLogger().warning("Unable to parse Version from String provided via plugin.yml!");
            return null;
        }
    }
}
