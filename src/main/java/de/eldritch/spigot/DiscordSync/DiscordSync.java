package de.eldritch.spigot.DiscordSync;

import de.eldritch.spigot.DiscordSync.discord.DiscordAPI;
import de.eldritch.spigot.DiscordSync.discord.DiscordConnectionException;
import de.eldritch.spigot.DiscordSync.module.ModuleManager;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.chat.ChatModule;
import de.eldritch.spigot.DiscordSync.module.emote.EmoteModule;
import de.eldritch.spigot.DiscordSync.module.name.NameModule;
import de.eldritch.spigot.DiscordSync.module.status.StatusModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Level;

public class DiscordSync extends JavaPlugin {
    public static DiscordSync singleton;

    private DiscordAPI    discordAPI;
    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        singleton = this;

        try {
            discordAPI = new DiscordAPI();
        } catch (DiscordConnectionException e) {
            getLogger().log(Level.SEVERE, "Unable to instantiate DiscordAPI.", e);
        }

        moduleManager = new ModuleManager(
                Map.entry(ChatModule.class, new Object[]{}),
                Map.entry(EmoteModule.class, new Object[]{}),
                Map.entry(NameModule.class, new Object[]{}),
                Map.entry(StatusModule.class, new Object[]{})
        );

        moduleManager.getRegisteredModules().forEach(PluginModule::onEnable);
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
}
