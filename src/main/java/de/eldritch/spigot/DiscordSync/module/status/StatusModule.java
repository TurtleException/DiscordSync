package de.eldritch.spigot.DiscordSync.module.status;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;

/**
 * https://github.com/TurtleException/TurtleFly/blob/main/src/main/java/de/eldritch/TurtleFly/module/status/StatusModule.java
 */
public class StatusModule extends PluginModule {
    private boolean offline = false;

    @Override
    public void onEnable() throws PluginModuleEnableException {
        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new PluginModuleEnableException("Module is dependant on JDA connection.");
    }

    @Override
    public void onDisable() {
        this.offline = true;
    }
}
