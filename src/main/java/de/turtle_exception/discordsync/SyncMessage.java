package de.turtle_exception.discordsync;

import de.turtle_exception.fancyformat.FormatText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SyncMessage implements Entity {
    private final long id;
    private final SyncUser author;
    private final FormatText content;
    private final long reference;
    private final @Nullable Long sourceSnowflake;

    public SyncMessage(long id, SyncUser author, FormatText content, long reference, @Nullable Long sourceSnowflake) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.reference = reference;
        this.sourceSnowflake = sourceSnowflake;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public @NotNull SyncUser getAuthor() {
        return author;
    }

    public @NotNull FormatText getContent() {
        return content;
    }

    public long getReference() {
        return reference;
    }

    public @Nullable Long getSourceSnowflake() {
        return sourceSnowflake;
    }
}
