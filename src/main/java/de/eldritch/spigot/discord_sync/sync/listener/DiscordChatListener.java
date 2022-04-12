package de.eldritch.spigot.discord_sync.sync.listener;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.entities.DiscordMessage;
import de.eldritch.spigot.discord_sync.entities.EntityBuilder;
import de.eldritch.spigot.discord_sync.sync.SynchronizationService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class DiscordChatListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().equals(event.getJDA().getSelfUser())) return;
        if (!event.isFromGuild())                                   return;

        FileConfiguration config = DiscordSync.singleton.getConfig();
        if (!event.getGuild().getId().equals(config.getString("guild")))             return;
        if (!event.getChannel().getId().equals(config.getString("channel.message"))) return;

        /* ----- ^^ GUARDS ^^ ----- */

        DiscordMessage message = EntityBuilder.newDiscordMessage(event.getMessage());
        SynchronizationService.handle(message);
    }
}
