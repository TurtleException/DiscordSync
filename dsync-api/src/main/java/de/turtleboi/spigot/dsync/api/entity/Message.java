package de.turtleboi.spigot.dsync.api.entity;

import de.turtleboi.fancyformat.FormatText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class Message {
    private final long id;
    private final @NotNull User author;
    private final @NotNull FormatText content;
    private final @Nullable Message replyTo;

    private final Map<String, Object> contextCache;

    public Message(long id, @NotNull User author, @NotNull FormatText content, @Nullable Message replyTo, @NotNull Map<String, Object> context) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.replyTo = replyTo;

        this.contextCache = Map.copyOf(context);
    }

    public long getId() {
        return this.id;
    }

    public @NotNull User getAuthor() {
        return this.author;
    }

    public @NotNull FormatText getContent() {
        return this.content;
    }

    public @Nullable Message getReplyTo() {
        return this.replyTo;
    }

    @SuppressWarnings("unchecked")
    public <T> T getContext(@NotNull String key) {
        return (T) this.contextCache.get(key);
    }
}
