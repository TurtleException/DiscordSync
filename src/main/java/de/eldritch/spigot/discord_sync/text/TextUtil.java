package de.eldritch.spigot.discord_sync.text;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import de.eldritch.spigot.discord_sync.DiscordSync;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Level;

public class TextUtil {
    private static final Object LOCK = new Object();
    private static TextUtil singleton;

    /**
     * A simple switch used to determine whether the singleton initialization has already failed once without succeeding
     * afterwards. This way the console will not be spammed with stacktraces everytime a translatable text is requested.
     * @see TextUtil#checkSingleton()
     */
    private static boolean notifiedFailure = false;

    private final TextService textService_game;
    private final TextService textService_plugin;

    private TextUtil() throws FileNotFoundException, NullPointerException, JsonIOException, JsonSyntaxException {
        textService_game = new TextService("lang" + File.separator + "minecraft");
        textService_plugin = new TextService("lang" + File.separator + "plugin");
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
                    notifiedFailure = false;
                } catch (FileNotFoundException | NullPointerException | JsonIOException | JsonSyntaxException e) {
                    // only notify once
                    if (notifiedFailure) {
                        DiscordSync.singleton.getLogger().log(Level.SEVERE, "Unable to initialize TextUtil singleton.", e);
                        notifiedFailure = true;
                    }
                }
            }
        }
    }

    /* ------------------------- */

    static @NotNull Text getFromPlugin(@NotNull String key, String... format) throws NullPointerException {
        checkSingleton();
        return singleton.textService_plugin.get(key, format);
    }

    static @NotNull Text getFromGame(@NotNull String key, String... format) throws NullPointerException {
        checkSingleton();
        return singleton.textService_game.get(key, format);
    }
}
