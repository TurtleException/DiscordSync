package de.turtleboi.spigot.dsync.core.chat;

import de.turtleboi.fancyformat.FormatText;
import de.turtleboi.spigot.dsync.api.entity.Message;
import de.turtleboi.spigot.dsync.api.entity.User;
import de.turtleboi.spigot.dsync.util.IdUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageBuilder {
    private User author;
    private Player authorAsPlayer;
    private String contentRaw;
    private FormatText content;
    private Message replyTo;

    private final Map<String, Object> context = new ConcurrentHashMap<>();

    public @NotNull Message build() throws IllegalArgumentException {
        final long id = IdUtil.newId("dsync-message");

        // TODO: checks

        return new Message(id, this.author, this.content, this.replyTo, this.context);
    }

    public User getAuthor() {
        return author;
    }

    public MessageBuilder setAuthor(User author) {
        this.author = author;
        return this;
    }

    public Player getAuthorAsPlayer() {
        return this.authorAsPlayer;
    }

    public MessageBuilder setAuthorAsPlayer(Player authorAsPlayer) {
        this.authorAsPlayer = authorAsPlayer;
        return this;
    }

    public String getContentRaw() {
        return this.contentRaw;
    }

    public MessageBuilder setContentRaw(String contentRaw) {
        this.contentRaw = contentRaw;
        return this;
    }

    public FormatText getContent() {
        return this.content;
    }

    public MessageBuilder setContent(FormatText content) {
        this.content = content;
        return this;
    }

    public Message getReplyTo() {
        return this.replyTo;
    }

    public MessageBuilder setReplyTo(Message replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public void setContext(@NotNull String key, Object val) {
        if (val == null)
            this.context.remove(key);
        else
            this.context.put(key, val);
    }
}
