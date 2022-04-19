package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.entities.interfaces.DiscordRepresentable;
import de.eldritch.spigot.discord_sync.entities.interfaces.DiscordSynchronizable;
import de.eldritch.spigot.discord_sync.entities.interfaces.Referencable;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.User;
import de.eldritch.spigot.discord_sync.util.format.MessageFormatter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinecraftMessage extends MinecraftSyncMessage implements DiscordSynchronizable {
    private long snowflake = -1;

    protected MinecraftMessage(long turtle, @NotNull User author, long timestamp, String content, @Nullable Referencable reference) {
        super(turtle, author, timestamp, content, reference);
    }

    @Override
    public String getLogMessage() {
        return "[%s] [MINECRAFT] <%s> %s".formatted(refNum, author.getEffectiveName(), content);
    }

    @Override
    public void sendToDiscord() {
        MessageAction msgAction = DiscordSync.singleton.getDiscordService().getAccessor().send(
                Accessor.Channel.MESSAGE,
                new MessageBuilder(
                        Text.of("channel.message", author.getEmote(), author.getEffectiveName(), MessageFormatter.formatDiscord(this)).content()
                ).build()
        );

        // reference message
        if (reference != null && reference instanceof DiscordRepresentable snowflakeContainer) {
            msgAction = msgAction
                    .referenceById(snowflakeContainer.getSnowflake())
                    .mentionRepliedUser(false);
        }

        msgAction.queue(
                message -> snowflake = message.getIdLong()
        );
    }

    // NOTE: The plugin is responsible for this to allow formatting and message events (e.g. click-reply)
    @Override
    public void sendToMinecraft() {
        sendToMinecraft("minecraft");
    }

    @Override
    public @NotNull Text getContainerText() {
        return Text.of("chat.reference.container", author.getEffectiveName(), String.valueOf(getID()), MessageFormatter.formatMinecraft(this));
    }

    @Override
    public long getSnowflake() throws IllegalStateException {
        if (snowflake < 0)
            throw new IllegalStateException("Message has not been sent yet.");
        return snowflake;
    }
}
