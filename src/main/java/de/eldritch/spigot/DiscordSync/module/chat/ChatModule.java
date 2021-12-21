package de.eldritch.spigot.DiscordSync.module.chat;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;
import de.eldritch.spigot.DiscordSync.module.chat.listener.DiscordListener;
import de.eldritch.spigot.DiscordSync.module.chat.listener.MinecraftEventListener;
import de.eldritch.spigot.DiscordSync.module.chat.listener.MinecraftJoinListener;
import de.eldritch.spigot.DiscordSync.module.chat.listener.MinecraftListener;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Mirrors the Minecraft chat with Discord and provides custom formatting.
 */
public class ChatModule extends PluginModule {
    private TextChannel textChannel;

    @Override
    public void onEnable() throws PluginModuleEnableException {
        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new PluginModuleEnableException("Module is dependant on JDA connection.");

        if (DiscordSync.singleton.getDiscordAPI().getGuild() == null)
            throw new PluginModuleEnableException("Guild cannot be null.");

        textChannel = DiscordSync.singleton.getDiscordAPI().getGuild().getTextChannelById(getConfig().getString("discord.channel", "null"));

        if (textChannel == null)
            throw new PluginModuleEnableException("Channel cannot be null.");

        DiscordSync.singleton.getServer().getPluginManager().registerEvents(new MinecraftEventListener(this), DiscordSync.singleton);
        DiscordSync.singleton.getServer().getPluginManager().registerEvents(new MinecraftListener(this), DiscordSync.singleton);
        DiscordSync.singleton.getServer().getPluginManager().registerEvents(new MinecraftJoinListener(), DiscordSync.singleton);

        DiscordSync.singleton.getDiscordAPI().getJDA().addEventListener(new DiscordListener(this));
    }


    /**
     * Passes a Discord message to Minecraft.
     * @param msg The source message.
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
                    // get target message and create reply
                    Objects.requireNonNull(this.getTextChannel().retrieveMessageById(msg.getReplyTarget())).complete().reply(msg.toDiscord()).queue(message -> {
                        msg.send(Objects.requireNonNull(Objects.requireNonNull(message.getReferencedMessage()).getMember()), message.getReferencedMessage());
                    });
                    return; // prevent the message from being sent separately
                } catch (IllegalArgumentException e) {
                    // The @ at the beginning was not a reply target
                    msg.send();
                    this.getTextChannel().sendMessage("@" + msg.getReplyTarget() + " " + msg.toDiscord()).queue();
                    return;
                } catch (NullPointerException e) {
                    msg.send();
                    DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to send discord message '" + msg.toDiscord() + "' as reply.", e);
                }
            }

            msg.send();
            this.getTextChannel().sendMessage(msg.toDiscord()).queue();
        } else {
            DiscordSync.singleton.getLogger().warning("Unable to send Discord message '" + msg.toDiscord() + "'.");
            msg.send();
        }
    }

    public void sendEmbed(MessageEmbed embed) {
        if (DiscordSync.singleton.getDiscordAPI() != null) {
            this.getTextChannel().sendMessageEmbeds(embed).queue();
        } else {
            DiscordSync.singleton.getLogger().warning("Unable to send Discord event message. (" + embed + ")");
        }
    }

    private @NotNull TextChannel getTextChannel() {
        return textChannel;
    }

    public static String getBustUrl(String name) {
        return "https://mc-heads.net/avatar/" + name;
    }
}