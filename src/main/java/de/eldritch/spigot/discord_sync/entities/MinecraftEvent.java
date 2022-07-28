package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.entities.interfaces.DiscordSynchronizable;
import de.eldritch.spigot.discord_sync.user.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.jetbrains.annotations.NotNull;

public abstract class MinecraftEvent implements DiscordSynchronizable {
    protected final long timestamp;

    protected final User user;

    protected final Accessor.Channel channel;
    protected final EmbedBuilder     builder;

    private long snowflake = -1;

    public MinecraftEvent(long timestamp, Accessor.Channel channel, @NotNull User user, EmbedBuilder builder) {
        this.timestamp = timestamp;
        this.channel = channel;
        this.builder = builder;
        this.user = user;
    }

    @Override
    public final void sendToDiscord() {
        DiscordSync.singleton.getDiscordService().getAccessor()
                .send(channel, new MessageBuilder(builder).build())
                .queue(message -> snowflake = message.getIdLong());
    }

    @Override
    public long getSnowflake() throws IllegalStateException {
        if (snowflake < 0)
            throw new IllegalStateException("Message has not been sent yet.");
        return snowflake;
    }
}
