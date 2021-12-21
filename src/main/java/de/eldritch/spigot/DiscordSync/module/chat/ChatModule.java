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

import java.util.*;
import java.util.logging.Level;

/**
 * Mirrors the Minecraft chat with Discord and provides custom formatting.
 */
public class ChatModule extends PluginModule {
    private TextChannel textChannel;

    public static final LinkedList<SynchronizedMinecraftMessage> CACHED_MESSAGES = new LinkedList<>();
    public static final LinkedList<Long> MINECRAFT_MESSAGE_IDS = new LinkedList<>();

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
        CACHED_MESSAGES.addFirst(msg);

        if (DiscordSync.singleton.getDiscordAPI() != null) {
            if (msg.getReplyTarget() != null) {
                String discordReplyTarget = msg.getReplyTarget();
                if (msg.getReplyTarget().startsWith("M")) {
                    for (SynchronizedMinecraftMessage cachedMessage : ChatModule.CACHED_MESSAGES) {
                        if (msg.getReplyTarget().equals("M" + cachedMessage.getId())) {
                            discordReplyTarget = "D" + cachedMessage.getDiscordMessageId();
                            break;
                        }
                    }
                }

                try {
                    // get target message and create reply
                    Objects.requireNonNull(this.getTextChannel().retrieveMessageById(Objects.requireNonNull(discordReplyTarget).substring(1))).complete().reply(msg.toDiscord(false)).queue(message -> {
                        msg.send(Objects.requireNonNull(Objects.requireNonNull(message.getReferencedMessage()).getMember()), message.getReferencedMessage());
                        msg.setDiscordMessageId(message.getId());
                    });
                    return; // prevent the message from being sent separately
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    // The @ at the beginning was not a reply target
                    msg.send();
                    this.getTextChannel().sendMessage(msg.toDiscord(true)).queue(message -> {
                        msg.setDiscordMessageId(message.getId());
                    });
                    return;
                } catch (NullPointerException e) {
                    msg.send();
                    DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to send discord message '" + msg.toDiscord(true) + "' as reply.", e);
                }
            }

            msg.send();
            this.getTextChannel().sendMessage(msg.toDiscord(false)).queue(message -> {
                msg.setDiscordMessageId(message.getId());
            });
        } else {
            DiscordSync.singleton.getLogger().warning("Unable to send Discord message '" + msg.toDiscord(true) + "'.");
            msg.send();
        }

        updateCache();
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

    /**
     * Updates the message cache and removes the oldest messages if it exceeds its limit of 100 messages. This limit was
     * chosen because Minecraft usually stores the last 100 messages.
     */
    public static void updateCache() {
        while (CACHED_MESSAGES.size() > 100) {
            CACHED_MESSAGES.removeLast();
        }

        while (MINECRAFT_MESSAGE_IDS.size() > 100) {
            MINECRAFT_MESSAGE_IDS.removeLast();
        }
    }

    public static String requestMinecraftId() {
        long instant = System.currentTimeMillis();

        while (MINECRAFT_MESSAGE_IDS.contains(instant))
            instant++;

        MINECRAFT_MESSAGE_IDS.addFirst(instant);
        return String.valueOf(instant);
    }
}