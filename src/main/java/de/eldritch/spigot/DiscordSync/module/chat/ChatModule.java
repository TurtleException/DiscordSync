package de.eldritch.spigot.DiscordSync.module.chat;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;

/**
 * https://github.com/TurtleException/TurtleFly/blob/main/src/main/java/de/eldritch/TurtleFly/module/chat/ChatModule.java
 * https://github.com/TurtleException/TurtleFly/blob/main/src/main/java/de/eldritch/TurtleFly/module/sync/SyncModule.java
 */
public class ChatModule extends PluginModule {
    public ChatModule() {
        super();
    }

    @Override
    public void onEnable() throws PluginModuleEnableException {
        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new PluginModuleEnableException("Module is dependant on JDA connection.");
    }
}
