package de.turtleboi.spigot.dsync.channel.endpoints;

import de.turtleboi.spigot.dsync.channel.Channel;
import de.turtleboi.spigot.dsync.channel.Endpoint;
import de.turtleboi.spigot.dsync.message.MessageEntity;
import de.turtleboi.spigot.dsync.message.SyncMessage;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class DiscordChannel extends Endpoint {
    private final long snowflake;

    public DiscordChannel(long id, @NotNull Channel channel, long snowflake) {
        super(id, channel);
        this.snowflake = snowflake;
    }

    public DiscordChannel(@NotNull Channel channel, long snowflake) {
        super(channel);
        this.snowflake = snowflake;
    }

    public long getSnowflake() {
        return snowflake;
    }

    @Override
    public void send(@NotNull MessageEntity message) {
        // ignore if the message came from this channel
        if (message.getSource().isFromDiscordChannel(snowflake)) return;

        MessageChannel discord = channel.getPlugin().getJDA().getChannelById(MessageChannel.class, snowflake);
        if (discord != null) {
            this.doSend(discord, message);
        } else {
            channel.getPlugin().getJDA().openPrivateChannelById(snowflake).queue(privateChannel -> {
                doSend(privateChannel, message);
            }, throwable -> {
                channel.getPlugin().getLogger().log(Level.WARNING, "Missing channel " + snowflake);
            });
        }
    }

    protected void doSend(@NotNull MessageChannel discord, @NotNull MessageEntity message) {
        String msg = message.toDiscord(discord);

        MessageCreateAction action = discord.sendMessage(
                new MessageCreateBuilder()
                        .setContent(msg)
                        .build());

        // get the discord response code (id of referenced message) for this specific channel
        if (message instanceof SyncMessage syncMessage) {
            Long reference = channel.getResponseCodes().get(snowflake).get(syncMessage.getReference());
            if (reference != null)
                action.setMessageReference(reference);
        }

        action.queue(success -> {
            channel.getResponseCodes().get(snowflake).put(message.getId(), success.getIdLong());
        }, throwable -> {
            channel.getPlugin().getLogger().log(Level.WARNING, "Encountered an unexpected exception while attempting to send message " + message.getId(), throwable);
        });
    }
}