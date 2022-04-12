package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.entities.interfaces.ContainerTextObject;
import de.eldritch.spigot.discord_sync.entities.interfaces.DiscordSynchronizable;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.User;
import de.eldritch.spigot.discord_sync.util.Checks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.jetbrains.annotations.NotNull;

public abstract class MinecraftEvent implements DiscordSynchronizable {
    protected final long timestamp;

    protected final User user;

    protected Accessor.Channel channel;
    protected EmbedBuilder builder = new EmbedBuilder();

    private long snowflake = -1;

    public MinecraftEvent(long timestamp, Accessor.Channel channel, @NotNull User user) {
        this.timestamp = timestamp;
        this.channel = channel;
        this.user = user;
    }

    protected abstract void initBuilder();

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
