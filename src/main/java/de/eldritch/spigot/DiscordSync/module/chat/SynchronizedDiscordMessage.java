package de.eldritch.spigot.DiscordSync.module.chat;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.Map;

public class SynchronizedDiscordMessage {
    private final Message message;
    private final Member author;

    public SynchronizedDiscordMessage(Message message, Member author) {
        this.message = message;
        this.author = author;
    }

    /**
     * Formats the message to be compatible with the Minecraft chat.
     */
    public BaseComponent[] toMinecraft() {
        // stripped content for now as markdown is not supported
        TextComponent content = new TextComponent(ChatColor.GRAY + message.getContentStripped());

        ComponentBuilder attachments = new ComponentBuilder();
        if (!message.getAttachments().isEmpty()) {
            attachments.color(ChatColor.DARK_GRAY);
        }
        if (message.getAttachments().stream().anyMatch(Message.Attachment::isImage)) {
            attachments.append(" [IMG]");
        }
        if (message.getAttachments().stream().anyMatch(Message.Attachment::isVideo)) {
            attachments.append(" [VID]");
        }
        if (message.getAttachments().stream().anyMatch(attachment -> !(attachment.isImage() || attachment.isVideo()))) {
            attachments.append(" [FILE]");
        }

        TextComponent fullMessage = new TextComponent(content, new TextComponent(attachments.create()));
        fullMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@D" + message.getId() + " "));
        fullMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("§7§oKlicke zum antworten."))));

        if (message.getReferencedMessage() != null) {
            Message replyTarget = message.getReferencedMessage();

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
                Member replyTargetAuthor = null;
                if (DiscordSync.singleton.getDiscordAPI().getGuild() != null) {
                    replyTargetAuthor = DiscordSync.singleton.getDiscordAPI().getGuild().getMember(replyTarget.getAuthor());
                }
                messageAuthor  = "§9" + (replyTargetAuthor != null ? replyTargetAuthor.getEffectiveName() : replyTarget.getAuthor().getName());
                messageId      = "D" + replyTarget.getId();
                messageContent = replyTarget.getContentStripped();
            }

            return new BaseComponent[]{
                    MessageService.get(
                            "module.chat.message.bare.discord.name",
                            author.getEffectiveName()
                    ),
                    MessageService.get(
                            "module.chat.message.bare.reply",
                            messageAuthor,
                            messageId,
                            messageContent
                    ),
                    fullMessage
            };
        } else {
            return new BaseComponent[]{
                    MessageService.get(
                            "module.chat.message.bare.discord.name",
                            author.getEffectiveName()
                    ),
                    fullMessage
            };
        }
    }

    /**
     * Strips markdown characters like *, **, __, ~~, ||.
     * The reason {@link Message#getContentStripped()} is not used here is because
     * some markdown characters can still be formatted in Minecraft.
     */
    private String replaceMarkdown(String str) {
        return str; // useless for now
    }

    /**
     * Replaces certain Emotes with raw text versions like :) or :c
     */
    private String replaceEmotes(String str) {
        return str; // useless for now
    }
}
