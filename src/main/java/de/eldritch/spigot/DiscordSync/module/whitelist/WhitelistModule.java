package de.eldritch.spigot.DiscordSync.module.whitelist;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;
import de.eldritch.spigot.DiscordSync.module.whitelist.listener.DiscordWhitelistListener;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.HashSet;

public class WhitelistModule extends PluginModule {
    private TextChannel channel;

    private HashSet<Request> queue;

    @Override
    public void onEnable() throws PluginModuleEnableException {
        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new PluginModuleEnableException("Module is dependant on JDA connection.");

        DiscordSync.singleton.getDiscordAPI().getJDA().addEventListener(new DiscordWhitelistListener(this));
        DiscordSync.singleton.getDiscordAPI().getJDA().upsertCommand(
                new CommandData("whitelist", "Erstelle eine Anfrage auf die Minecraft Whitelist zu kommen.")
                        .addOption(OptionType.STRING, "Name", "Dein Minecraft-Name", true)
        ).queue();

        queue = new HashSet<>();

        this.reloadChannel();
    }

    @Override
    public void onDisable() {

    }

    public void reloadChannel() {
        if (DiscordSync.singleton.getDiscordAPI() != null && DiscordSync.singleton.getDiscordAPI().getGuild() != null) {
            try {
                this.channel = DiscordSync.singleton.getDiscordAPI().getGuild().getTextChannelById(
                        getConfig().isLong("channel") ? getConfig().getLong("channel") : Long.parseLong(getConfig().getString("channel", "null"))
                );
            } catch (NumberFormatException e) {
                getLogger().warning("Property 'channel' of module '" + getName() + "' should be of type long");
            }
        }
    }

    public void request(Request request) {
        queue.add(request);
    }

    public TextChannel getChannel() {
        return channel;
    }
}
