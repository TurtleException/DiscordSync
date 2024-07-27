package de.turtleboi.spigot.dsync.api.entity;

import de.turtleboi.fancyformat.FormatText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Message {
    private final long id;
    private final @NotNull User author;
    private final @NotNull FormatText content;
    private final @Nullable Message replyTo;

    private final Map<String, Object> contextCache = new ConcurrentHashMap<>();

    public Message(long id, @NotNull User author, @NotNull FormatText content, @Nullable Message replyTo, @NotNull Object... context) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.replyTo = replyTo;

        if (context.length % 2 != 0)
            throw new IllegalArgumentException("Must provide an even amount of context arguments");

        for (int i = 0; i < context.length; i = i + 2) {
            String key = String.valueOf(context[i]);

            contextCache.put(key, context[i + 1]);
        }
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
