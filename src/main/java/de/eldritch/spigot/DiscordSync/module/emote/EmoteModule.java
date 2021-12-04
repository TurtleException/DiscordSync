package de.eldritch.spigot.DiscordSync.module.emote;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class EmoteModule extends PluginModule {
    @Override
    public void onEnable() throws PluginModuleEnableException {
        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new PluginModuleEnableException("Module is dependant on JDA connection.");

        DiscordSync.singleton.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), DiscordSync.singleton);
    }

    /**
     * Retrieves the avatar (face of the skin) of a {@link Player}
     * via <code>https://minotar.net</code>.
     * @param name The name of the {@link Player}.
     * @return Avatar as a byte array.
     * @throws IOException if something goes wrong while requesting
     *                     the avatar.
     */
    public static byte[] retrieveAvatar(String name) throws IOException {
        URL url = new URL("https://minotar.net/helm/" + name + "/256");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (InputStream in = url.openStream()) {
            int n = 0;
            byte[] buffer = new byte[1024];
            while (-1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
            }
        }

        return out.toByteArray();
    }
}
