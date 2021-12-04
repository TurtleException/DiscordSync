package de.eldritch.spigot.DiscordSync.module.chat;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class SynchronizedDiscordMessage {
    private Message message;
    private Member author;

    public SynchronizedDiscordMessage(Message message, Member author) {
        this.message = message;
        this.author = author;
    }

    /**
     * Formats the message to be compatible with the Minecraft chat.
     */
    public TextComponent toMinecraft() {
        TextComponent name = new TextComponent(
                (author.getUser().isBot() || author.getUser().isSystem() ? ChatColor.GRAY : ChatColor.BLUE)
                        + ((author.getNickname() != null) ? author.getNickname() : author.getUser().getName())
                        + ChatColor.DARK_GRAY + ": " );
        name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                ((author.getColor() != null) ? ChatColor.of(author.getColor()) : ChatColor.BLUE) + author.getEffectiveName() + "#" + author.getUser().getDiscriminator()
        )));

        // stripped content for now as markdown is not supported
        TextComponent content = new TextComponent(" " + ChatColor.GRAY + message.getContentStripped());
        content.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + message.getId() + " "));
        content.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("" + ChatColor.ITALIC + ChatColor.GRAY + "Klicke zum antworten.")));

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

        return new TextComponent(name, content, new TextComponent(attachments.create()));
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
