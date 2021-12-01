package de.eldritch.spigot.DiscordSync.module;

public class PluginModuleEnableException extends Exception {
    public PluginModuleEnableException(String message) {
        super(message);
    }

    public PluginModuleEnableException(String message, Throwable cause) {
        super(message, cause);
    }
}
