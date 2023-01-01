package de.turtle_exception.discordsync.listeners;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.message.source.Author;
import de.turtle_exception.discordsync.message.SyncMessage;
import de.turtle_exception.discordsync.SyncUser;
import de.turtle_exception.discordsync.channel.Channel;
import de.turtle_exception.discordsync.util.time.TurtleType;
import de.turtle_exception.discordsync.util.time.TurtleUtil;
import de.turtle_exception.fancyformat.FormatText;
import de.turtle_exception.fancyformat.formats.DiscordFormat;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
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
        if (event.getAuthor().getIdLong() == plugin.getJDA().getSelfUser().getIdLong()) return;

        long discord = event.getChannel().getIdLong();

        if (event.getChannel() instanceof PrivateChannel pChannel) {
            User privateAuthor = pChannel.getUser();
            if (privateAuthor != null)
                discord = privateAuthor.getIdLong();
        }

        for (Channel channel : plugin.getChannelCache()) {
            if (!channel.getSnowflakes().contains(discord)) continue;

            String name = event.getMember() != null
                    ? event.getMember().getEffectiveName()
                    : event.getAuthor().getName();

            SyncUser author = plugin.getUser(event.getAuthor().getIdLong());
            if (author == null)
                author = plugin.putUser(event.getAuthor().getIdLong(), name);

            // TODO: reference

            Member member = event.getMember();
            User   user   = event.getAuthor();

            Author      source  = new Author(user, member, event.getChannel());
            FormatText  content = plugin.getFormatter().fromFormat(event.getMessage().getContentRaw(), DiscordFormat.get());
            SyncMessage message = new SyncMessage(plugin, TurtleUtil.newId(TurtleType.MESSAGE), author, content, -1, source);

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
            author = plugin.putUser(uuid, event.getPlayer().getDisplayName());

        // TODO: reference

        // players use Discord markdown
        FormatText  content = plugin.getFormatter().fromFormat(event.getMessage(), DiscordFormat.get());

        Author      source  = new Author(event.getPlayer(), event.getPlayer().getWorld());
        SyncMessage message = new SyncMessage(plugin, TurtleUtil.newId(TurtleType.MESSAGE), author, content, -1, source);
        Channel     channel = plugin.getChannel(event.getPlayer());

        channel.send(message);
    }
}
