package de.eldritch.spigot.DiscordSync.module.whitelist;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;
import de.eldritch.spigot.DiscordSync.module.whitelist.listener.DiscordWhitelistListener;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.HashSet;
import java.util.logging.Level;

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

    /**
     * Attempts to retrieve all currently issued whitelist requests from the Discord {@link TextChannel}.
     */
    private void retrieveForeignQueue() {
        try {
            if (DiscordSync.singleton.getDiscordAPI() != null
                    && DiscordSync.singleton.getDiscordAPI().getGuild() != null
                    && channel != null) {
                int limit = getConfig().isInt("cache-limit") ? getConfig().getInt("cache-limit") : Integer.parseInt(getConfig().getString("cache-limit", "null"));

                getLogger().info("Attempting to retrieve " + limit + " messages from channel history.");
                for (final int[] i = {1}; i[0] <= limit; i[0]++) {
                    channel.getHistory().retrievePast(1).queue(messages -> {
                        if (messages.isEmpty()) {
                            i[0] = limit + 1;
                        } else {
                            Message message = messages.get(0);
                            Request request = Request.from(message);

                            if (request == null) {
                                // request is invalid
                                message.addReaction("U+26A0").queue();      // warning           -> invalid
                                message.removeReaction("U+1F7E2").queue();  // green circle      -> accept
                                message.removeReaction("U+1F534").queue();  // red circle        -> deny
                                message.removeReaction("U+2705").queue();   // check mark button -> accepted
                                message.removeReaction("U+274C").queue();   // cross mark        -> denied
                                getLogger().info("Request " + message.getId() + " denied: INVALID");
                            } else {
                                // request is valid
                                // TODO
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Encountered exception when retrieving foreign queue", e);
        }
    }

    public void request(Request request) {
        queue.add(request);
    }

    public TextChannel getChannel() {
        return channel;
    }
}
