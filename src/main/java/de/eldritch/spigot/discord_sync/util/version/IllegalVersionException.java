package de.eldritch.spigot.discord_sync.util.version;

public class IllegalVersionException extends RuntimeException {
    IllegalVersionException(String raw, Throwable cause) {
        super("'" + raw + "' could not be converted to Version", cause);
    }

    public IllegalVersionException(String msg) {
        super(msg);
    }
}
