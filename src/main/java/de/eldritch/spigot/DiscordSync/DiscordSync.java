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
import de.eldritch.spigot.DiscordSync.util.version.IllegalVersionException;
import de.eldritch.spigot.DiscordSync.util.Performance;
import de.eldritch.spigot.DiscordSync.util.version.Version;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
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

    /**
     * @return Default plugin chat prefix.
     */
    public static TextComponent getChatPrefix() {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append("[")
                .color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
                .append("SERVER")
                .color(net.md_5.bungee.api.ChatColor.GREEN)
                .append("] ")
                .color(net.md_5.bungee.api.ChatColor.DARK_GRAY);

        TextComponent component = new TextComponent(builder.create());
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(ChatColor.GREEN + "TurtleFly\n" + ChatColor.ITALIC.toString() + ChatColor.GRAY.toString() + "v" + singleton.getDescription().getVersion())
        ));

        return new TextComponent(component);
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
