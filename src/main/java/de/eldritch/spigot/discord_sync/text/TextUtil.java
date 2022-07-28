package de.eldritch.spigot.discord_sync.text;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import de.eldritch.spigot.discord_sync.DiscordSync;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;

public class TextUtil {
    private static final Object LOCK = new Object();
    private static TextUtil singleton;

    private final TextService textService_game;
    private final TextService textService_plugin;

    private TextUtil() throws JsonIOException, JsonSyntaxException, IOException {
        textService_game = new TextService("lang/minecraft", false);
        textService_plugin = new TextService("lang/plugin", true);

        final String language = DiscordSync.singleton.getConfig().getString("language", TextService.DEFAULT_LANGUAGE);
        textService_game.load(language);
        textService_plugin.load(language);
    }

    /**
     * Simple helper method to ensure that the singleton has been initialized.
     */
    public static void init() {
        checkSingleton();
    }

    private static void checkSingleton() {
        synchronized (LOCK) {
            if (singleton == null) {
                try {
                    singleton = new TextUtil();
                } catch (JsonIOException | JsonSyntaxException | IOException e) {
                    DiscordSync.singleton.getLogger().log(Level.SEVERE, "Unable to initialize TextUtil singleton.", e);
                }
            }
        }
    }

    /* ------------------------- */

    static @NotNull Text getFromPlugin(@NotNull String key, String... format) throws NullPointerException {
        checkSingleton();
        return TextUtil.singleton.textService_plugin.get(key, format);
    }

    static @NotNull Text getFromGame(@NotNull String key, String... format) throws NullPointerException {
        checkSingleton();
        return TextUtil.singleton.textService_game.get(key, format);
    }
}
