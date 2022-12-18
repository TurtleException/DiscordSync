package de.turtle_exception.discordsync.listeners;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.SyncMessage;
import de.turtle_exception.discordsync.SyncUser;
import de.turtle_exception.discordsync.channel.Channel;
import de.turtle_exception.discordsync.util.time.TurtleType;
import de.turtle_exception.discordsync.util.time.TurtleUtil;
import de.turtle_exception.fancyformat.Format;
import de.turtle_exception.fancyformat.FormatText;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatListener extends ListenerAdapter implements Listener {
    private final DiscordSync plugin;

    public ChatListener(@NotNull DiscordSync plugin) {
        this.plugin = plugin;
    }

    /* - DISCORD - */

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        for (Channel channel : plugin.getChannelCache()) {
            if (!channel.getSnowflakes().contains(event.getChannel().getIdLong())) continue;

            SyncUser author = plugin.getUser(event.getAuthor().getIdLong());
            if (author == null)
                author = plugin.putUser(event.getAuthor().getIdLong());

            // TODO: reference

            FormatText  content = plugin.getFormatter().newText(event.getMessage().getContentRaw(), Format.DISCORD);
            SyncMessage message = new SyncMessage(TurtleUtil.newId(TurtleType.MESSAGE), author, content, -1, event.getChannel().getIdLong());

            channel.send(message);
            return;
        }
    }

    /* - MINECRAFT - */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(@NotNull AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        UUID uuid = event.getPlayer().getUniqueId();

        SyncUser author = plugin.getUser(uuid);
        if (author == null)
            author = plugin.putUser(uuid);

        // TODO: reference

        // players use Discord markdown
        FormatText  content = plugin.getFormatter().newText(event.getMessage(), Format.DISCORD);

        SyncMessage message = new SyncMessage(TurtleUtil.newId(TurtleType.MESSAGE), author, content, -1, null);
        Channel     channel = plugin.getChannelMapper().get(uuid);

        channel.send(message);
    }
}
