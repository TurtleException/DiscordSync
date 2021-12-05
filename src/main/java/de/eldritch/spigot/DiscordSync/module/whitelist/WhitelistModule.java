package de.eldritch.spigot.DiscordSync.module.whitelist;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;
import de.eldritch.spigot.DiscordSync.module.whitelist.listener.DiscordWhitelistListener;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Level;

public class WhitelistModule extends PluginModule {
    private TextChannel channel;
    private long commandId = 0;

    private HashSet<WhitelistRequest> queue;

    private final YamlConfiguration requestYaml = new YamlConfiguration();
    private File requestYamlFile;

    @Override
    public void onEnable() throws PluginModuleEnableException {
        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new PluginModuleEnableException("Module is dependant on JDA connection.");

        if (DiscordSync.singleton.getDiscordAPI().getGuild() == null)
            throw new PluginModuleEnableException(new NullPointerException("Guild cannot be null"));


        File[] files = DiscordSync.singleton.getDataFolder().listFiles((dir, name) -> name.equals("requests.yml"));
        if (files == null || files.length == 0) {
            DiscordSync.singleton.getLogger().info("requests.yml does not exist. Attempting to create a new file...");
            try {
                if (!DiscordSync.singleton.getDataFolder().exists() && !DiscordSync.singleton.getDataFolder().mkdir())
                    throw new IOException("Could not create plugin data folder.");
                new File(DiscordSync.singleton.getDataFolder(), "requests.yml").createNewFile();

                DiscordSync.singleton.getLogger().info("requests.yml created!");
            } catch (IOException e) {
                throw new PluginModuleEnableException("Unable to access requests.yml.", e);
            }
        }

        try {
            requestYamlFile = Objects.requireNonNull(files)[0];
            requestYaml.load(requestYamlFile);
        } catch (IOException | InvalidConfigurationException | ArrayIndexOutOfBoundsException e) {
            throw new PluginModuleEnableException("Unable to load requests.yml", e);
        }


        DiscordSync.singleton.getDiscordAPI().getGuild().upsertCommand(
                new CommandData("whitelist", "Erstelle eine Anfrage auf die Minecraft Whitelist zu kommen.")
                        .addOption(OptionType.STRING, "name", "Dein Minecraft-Name", true)
        ).queue(command -> {
            commandId = command.getIdLong();
            getLogger().log(Level.INFO, "Successfully created command 'whitelist' (" + command.getId() + ")");
        }, throwable -> {
            getLogger().log(Level.WARNING, "Unable to create command 'whitelist'", throwable);
        });
        DiscordSync.singleton.getDiscordAPI().getJDA().addEventListener(new DiscordWhitelistListener(this));

        queue = new HashSet<>();

        this.reloadChannel();
    }

    @Override
    public void onDisable() {
        if (DiscordSync.singleton.getDiscordAPI().getGuild() != null) {
            DiscordSync.singleton.getDiscordAPI().getGuild().deleteCommandById(commandId).queue(unused -> {
                getLogger().info("Deleted command 'whitelist' (" + commandId + ")");
            }, throwable -> {
                getLogger().log(Level.WARNING, "Unable to delete command 'whitelist' (" + commandId + ")", throwable);
            });
        }
    }

    public void reloadChannel() {
        if (DiscordSync.singleton.getDiscordAPI() != null && DiscordSync.singleton.getDiscordAPI().getGuild() != null) {
            try {
                this.channel = DiscordSync.singleton.getDiscordAPI().getGuild().getTextChannelById(
                        getConfig().isLong("channel") ? getConfig().getLong("channel") : Long.parseLong(getConfig().getString("channel", "null"))
                );
            } catch (NumberFormatException e) {
                getLogger().log(Level.WARNING, "Property 'channel' (" + getConfig().getString("channel", "null") + ") should be of type long", e);
            }
        }
    }

    public void request(WhitelistRequest request) {
        requestYaml.set(request.getMember().getId(), request.getUuid().toString());
        try {
            requestYaml.save(requestYamlFile);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Unable to save requests.yml", e);
        }

        queue.add(request);
    }

    public TextChannel getChannel() {
        return channel;
    }
}
