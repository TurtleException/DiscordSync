package de.eldritch.spigot.discord_sync.util;

import de.eldritch.spigot.discord_sync.DiscordSync;

/**
 * Represents the plugin status.
 */
public enum Status {
    /**
     * The plugin is being initialized but {@link DiscordSync#onEnable()} has not been invoked yet.
     */
    PRE_INIT,
    /**
     * The plugin is starting. {@link DiscordSync#onEnable()} has been invoked.
     */
    STARTING,
    /**
     * The plugin has been started and is currently awaiting listener calls or commands. This will be the active status
     * for most of the time.
     */
    RUNNING,
    /**
     * The plugin is being stopped. {@link DiscordSync#onDisable()} has been invoked and the plugin is shutting down.
     */
    STOPPING,
    /**
     * The plugin has been stopped. All listeners should be unregistered or inactive.
     */
    STOPPED
}
