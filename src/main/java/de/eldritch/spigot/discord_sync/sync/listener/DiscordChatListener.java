package de.eldritch.spigot.discord_sync.sync.listener;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.entities.DiscordMessage;
import de.eldritch.spigot.discord_sync.entities.EntityBuilder;
import de.eldritch.spigot.discord_sync.sync.SynchronizationService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordChatListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;

        if (!event.isFromGuild()) return;

        Accessor discordAccessor = DiscordSync.singleton.getDiscordService().getAccessor();
        if (!event.getGuild().getId().equals(discordAccessor.getGuild().getId()))            return;
        if (!event.getChannel().getId().equals(discordAccessor.getMessageChannel().getId())) return;

        /* ----- ^^ GUARDS ^^ ----- */

        DiscordMessage message = EntityBuilder.newDiscordMessage(event.getMessage());
        SynchronizationService.handle(message);
    }
}
