package de.eldritch.spigot.DiscordSync.module.chat;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

public class SynchronizedMinecraftMessage {
    private final String message;
    private final Player author;

    private final String id = ChatModule.requestMinecraftId();

    private String replyTarget = null;

    private String discordMessageId = null;

    public SynchronizedMinecraftMessage(String message, Player author) {
        this.author = author;
        String processedMessage = message;


        String[] tokens = message.split(" ");

        // remove response-prefix from actual message
        if (tokens.length > 0 && Pattern.compile("^@[D|M][0-9]+\s").matcher(message).find()) {
            processedMessage = message.substring(tokens[0].length() + " ".length());
            replyTarget = tokens[0].substring(1);
        }

        this.message = processedMessage;
    }

    /**
     * Formats the message to be compatible with
     * the Discord chat.
     */
    public String toDiscord(boolean appendPrefix) {
        String emote = "";
        if (DiscordSync.singleton.getDiscordAPI() != null) {
            try {
                emote = Objects.requireNonNull(Objects.requireNonNull(DiscordSync.singleton.getDiscordAPI()).getGuild()).getEmotesByName(author.getName(), false).get(0).getAsMention() + " ";
            } catch (NullPointerException | IndexOutOfBoundsException ignored1) {
                try {
                    emote = Objects.requireNonNull(DiscordSync.singleton.getDiscordAPI().getGuild()).getEmotesByName("minecraft", true).get(0).getAsMention() + " ";
                } catch (NullPointerException | IndexOutOfBoundsException ignored2) {
                    emote = "";
                }
            }
        }

        String str = emote + "__**" + author.getDisplayName() + "**:__  " + (appendPrefix ? ("@" + replyTarget + " ") : "") + message;

        if (str.length() >= 2000)
            str = str.substring(0, 1996) + "...";

        return str;
    }

    public void send() {
        DiscordSync.singleton.getServer().spigot().broadcast(
                MessageService.get(
                        "module.chat.message.bare.minecraft.name",
                        author.getDisplayName()
                ),
                getMessageComponent()
        );
    }

    public void send(@NotNull Member replyTargetAuthor, @NotNull Message replyTarget) {
        String messageAuthor, messageId, messageContent;

        // check whether the referenced message was sent by the bot (as a synchronized message)
        SynchronizedMinecraftMessage actualReplyTarget = null;
        for (SynchronizedMinecraftMessage cachedMessage : ChatModule.CACHED_MESSAGES) {
            if (replyTarget.getId().equals(cachedMessage.getDiscordMessageId())) {
                actualReplyTarget = cachedMessage;
                break;
            }
        }

        if (actualReplyTarget != null) {
            /* -> the reply target is a minecraft message */
            messageAuthor  = "§6" + actualReplyTarget.getAuthor().getDisplayName();
            messageId      = "M" + actualReplyTarget.getId();
            messageContent = actualReplyTarget.getMessage();

            ChatModule.updateCache();
        } else {
            /* -> the reply target is a discord message */
            messageAuthor  = "§9" + replyTargetAuthor.getEffectiveName();
            messageId      = "D" + replyTarget.getId();
            messageContent = replyTarget.getContentStripped();
        }


        DiscordSync.singleton.getServer().spigot().broadcast(
                MessageService.get(
                        "module.chat.message.bare.minecraft.name",
                        author.getDisplayName()
                ),
                MessageService.get(
                        "module.chat.message.bare.reply",
                        messageAuthor,
                        messageId,
                        formatNewline(messageContent)
                ),
                getMessageComponent()
        );
    }

    private TextComponent getMessageComponent() {
        TextComponent messageComponent = MessageService.get(
                "module.chat.message.bare.message",
                message
        );

        messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@M" + id + " "));
        messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("§7§oKlicke zum antworten."))));

        return messageComponent;
    }

    /**
     * @return Message id of the reply target, <code>null</code>
     *         if the message is not a reply.
     */
    public @Nullable String getReplyTarget() {
        return this.replyTarget;
    }

    public @Nullable String getDiscordMessageId() {
        return discordMessageId;
    }

    public void setDiscordMessageId(String discordMessageId) {
        this.discordMessageId = discordMessageId;
    }

    public String getMessage() {
        return message;
    }

    public Player getAuthor() {
        return author;
    }

    public String getId() {
        return id;
    }

    private String formatNewline(String message) {
        return WordUtils.wrap(message, 75, "\n", true);
    }
}
