package de.eldritch.spigot.DiscordSync.module.chat.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.chat.ChatModule;
import de.eldritch.spigot.DiscordSync.module.chat.SynchronizedDiscordMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class DiscordListener extends ListenerAdapter {
    private ChatModule module;

    public DiscordListener(ChatModule module) {
        this.module = module;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()
                || !event.getChannel().getId().equals(module.getConfig().getString("discord.channel"))
                || event.getAuthor().equals(event.getJDA().getSelfUser()))
            return;

        DiscordSync.singleton.getLogger().info("[DISCORD] "
                + event.getAuthor().getName() + ": "
                + event.getMessage().getContentRaw()
        );

        SynchronizedDiscordMessage discordMessage = new SynchronizedDiscordMessage(event.getMessage(), event.getMember());
        module.process(discordMessage);
    }


}