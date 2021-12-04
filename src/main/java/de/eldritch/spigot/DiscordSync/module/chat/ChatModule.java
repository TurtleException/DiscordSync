package de.eldritch.spigot.DiscordSync.module.chat;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;
import de.eldritch.spigot.DiscordSync.module.chat.listener.DiscordListener;
import de.eldritch.spigot.DiscordSync.module.chat.listener.MinecraftEventListener;
import de.eldritch.spigot.DiscordSync.module.chat.listener.MinecraftJoinListener;
import de.eldritch.spigot.DiscordSync.module.chat.listener.MinecraftListener;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Mirrors the Minecraft chat with Discord and provides custom formatting.
 */
public class ChatModule extends PluginModule {
    @Override
    public void onEnable() throws PluginModuleEnableException {
        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new PluginModuleEnableException("Module is dependant on JDA connection.");

        // minecraft listener
        DiscordSync.singleton.getServer().getPluginManager().registerEvents(new MinecraftEventListener(this), DiscordSync.singleton);
        DiscordSync.singleton.getServer().getPluginManager().registerEvents(new MinecraftListener(this), DiscordSync.singleton);
        DiscordSync.singleton.getServer().getPluginManager().registerEvents(new MinecraftJoinListener(), DiscordSync.singleton);

        // discord listener
        Objects.requireNonNull(DiscordSync.singleton.getDiscordAPI()).getJDA().addEventListener(new DiscordListener(this));
    }


    /**
     * Passes a Discord message to Minecraft.
     * @param msg
     */
    public void process(SynchronizedDiscordMessage msg) {
        DiscordSync.singleton.getServer().spigot().broadcast(msg.toMinecraft());
    }

    /**
     * Passes a Minecraft message to Discord.
     */
    public void process(SynchronizedMinecraftMessage msg) {
        if (DiscordSync.singleton.getDiscordAPI() != null) {
            if (msg.getReplyTarget() != null) {
                try {
                    // get target message and create replay
                    Objects.requireNonNull(this.getTextChannel().retrieveMessageById(msg.getReplyTarget())).complete().reply(msg.toDiscord()).queue();
                    return; // prevent the message from being sent separately
                } catch (NullPointerException e) {
                    DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to send discord message '" + msg.toDiscord() + "' as reply.", e);
                }
            }

            this.getTextChannel().sendMessage(msg.toDiscord()).queue();
        } else {
            DiscordSync.singleton.getLogger().warning("Unable to send Discord message '" + msg.toDiscord() + "'.");
        }
    }

    public void sendEmbed(MessageEmbed embed) {
        if (DiscordSync.singleton.getDiscordAPI() != null) {
            this.getTextChannel().sendMessageEmbeds(embed).queue();
        } else {
            DiscordSync.singleton.getLogger().warning("Unable to send Discord event message. (" + embed + ")");
        }
    }

    private TextChannel getTextChannel() {
        return Objects.requireNonNull(DiscordSync.singleton.getDiscordAPI()).getGuild().getTextChannelById(getConfig().getString("discord.textChannel", "null"));
    }

    public static String getBustUrl(String name) {
        return "https://minotar.net/armor/bust/" + name;
    }
}