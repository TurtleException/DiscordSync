package de.eldritch.spigot.discord_sync.util.version;

/**
 * Indicates that a String is not a valid representation of a {@link Version}.
 */
public class IllegalVersionException extends RuntimeException {
    IllegalVersionException(String raw, Throwable cause) {
        super("'" + raw + "' could not be converted to Version", cause);
    }

    public IllegalVersionException(String msg) {
        super(msg);
    }
}
