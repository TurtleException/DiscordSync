package de.turtle_exception.discordsync;

import de.turtle_exception.fancyformat.FormatText;
import org.jetbrains.annotations.NotNull;

public record SyncMessage(
        long id,
        @NotNull SyncUser author,
        @NotNull FormatText content,
        long reference,
        @NotNull SourceInfo sourceInfo
) implements Entity {
    @Override
    public long getId() {
        return id;
    }
}
