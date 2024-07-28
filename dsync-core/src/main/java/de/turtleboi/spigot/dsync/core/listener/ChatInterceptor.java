package de.turtleboi.spigot.dsync.core.listener;

import de.turtleboi.fancyformat.Format;
import de.turtleboi.fancyformat.FormatText;
import de.turtleboi.fancyformat.format.DiscordMarkdownFormat;
import de.turtleboi.spigot.dsync.api.DiscordSyncAPI;
import de.turtleboi.spigot.dsync.api.chat.MessageHistory;
import de.turtleboi.spigot.dsync.api.chat.MessageRouter;
import de.turtleboi.spigot.dsync.api.entity.Message;
import de.turtleboi.spigot.dsync.api.entity.User;
import de.turtleboi.spigot.dsync.core.chat.MessageBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class ChatInterceptor implements Listener {
    private final Format<String> format = new DiscordMarkdownFormat();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(@NotNull AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();

        DiscordSyncAPI api = DiscordSyncAPI.getInstance();

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setContentRaw(event.getMessage());
        messageBuilder.setAuthorAsPlayer(player);

        User author = api.getUserDAO().provideUserByMinecraft(player.getUniqueId(), player.getName());
        messageBuilder.setAuthor(author);

        resolveReplyTo(messageBuilder);

        FormatText content = new FormatText(messageBuilder.getContentRaw(), format);
        messageBuilder.setContent(content);

        MessageRouter router = api.getMessageRouter();
        router.handle(messageBuilder.build());
    }

    public static void resolveReplyTo(@NotNull MessageBuilder messageBuilder) {
        String contentRaw = messageBuilder.getContentRaw();

        // do nothing if the message does not start with "@"
        if (!contentRaw.startsWith("@")) return;

        String[] tokens = contentRaw.split(" ");

        // do nothing if the message would be empty
        if (tokens.length < 2)   return;
        if (tokens[1].isEmpty()) return;

        // parse id
        String idStr = tokens[0].substring(1); // id without "@"
        long   id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            // do nothing if the id could not be parsed (might just be a regular message that starts with "@")
            return;
        }

        DiscordSyncAPI api = DiscordSyncAPI.getInstance();
        MessageRouter router = api.getMessageRouter();
        MessageHistory history = router.getHistory();

        for (Message message : history) {
            if (message.getId() != id) continue;

            // message found!

            messageBuilder.setReplyTo(message);

            // trim "@<id> " from message
            String newMsg = contentRaw.substring(("@" + idStr + " ").length());
            messageBuilder.setContentRaw(newMsg);

            return;
        }

        /*
         * ID does not match any message. This might be because...
         * - ... it is not referencing a message at all
         * - ... the referenced message is too old (might be supported in future versions)
         */
    }
}
