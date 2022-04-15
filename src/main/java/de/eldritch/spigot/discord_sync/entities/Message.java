package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.entities.interfaces.Referencable;
import de.eldritch.spigot.discord_sync.entities.interfaces.Synchronizable;
import de.eldritch.spigot.discord_sync.entities.interfaces.Turtle;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Message implements Turtle, Referencable, Synchronizable {
    /**
     * Unique TurtleID.
     */
    private final long turtle;

    /**
     * Synchronized author object.
     */
    protected final User author;

    /**
     * UNIX timestamp
     */
    protected final long timestamp;

    /**
     * Raw content of the message.
     */
    protected final String content;

    /**
     * The referenced message this message is a reply to.
     */
    protected final @Nullable Referencable reference;

    protected String refNum;

    protected Message(long turtle, @NotNull User author, long timestamp, String content, @Nullable Referencable reference) {
        this.turtle = turtle;
        this.author = author;
        this.timestamp = timestamp;
        this.content = content;
        this.reference = reference;
    }

    @Override
    public final long getID() {
        return turtle;
    }

    @Override
    public void setRefNum(@NotNull String refNum) {
        this.refNum = refNum;
    }

    @Override
    public @NotNull String getRefNum() throws IllegalStateException {
        if (refNum == null)
            throw new IllegalStateException("Message has not been sent yet.");
        return refNum;
    }

    @Override
    public @NotNull Text getContainerText() {
        return Text.of("chat.reference.container", author.getName(), String.valueOf(getID()), getFormat());
    }

    public abstract @NotNull String getFormat();

    public String getContent() {
        return content;
    }
}
