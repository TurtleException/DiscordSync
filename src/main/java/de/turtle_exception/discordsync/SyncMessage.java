package de.turtle_exception.discordsync;

import de.turtle_exception.fancyformat.FormatText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SyncMessage(
        long id,
        @NotNull SyncUser author,
        @NotNull FormatText content,
        long reference,
        @Nullable Long sourceSnowflake
) implements Entity { }
