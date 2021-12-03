package de.eldritch.spigot.DiscordSync.util;

public class IllegalVersionException extends Exception {
    public IllegalVersionException(String raw, Throwable cause) {
        super("'" + raw + "' could not be converted to Version", cause);
    }

    public IllegalVersionException(String msg) {
        super(msg);
    }
}