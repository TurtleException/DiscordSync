package de.eldritch.spigot.discord_sync.text;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.eldritch.spigot.discord_sync.DiscordSync;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.MessageFormat;
import java.util.HashMap;

public class TextService {
    private static final String DEFAULT_LANGUAGE = "en_US";

    /**
     * The path this TextService will find lang files in.
     */
    private final String path;

    /**
     * The currently set language.
     */
    private String language;
    /**
     * Map of all the keys and their content in the currently set language.
     */
    private HashMap<String, String> langMap;

    /**
     * Synchronization lock.
     */
    private final Object lock = new Object();

    public TextService(@NotNull String path) throws FileNotFoundException, NullPointerException, JsonIOException, JsonSyntaxException {
        this.path = path;

        this.load(DEFAULT_LANGUAGE);
    }

    /* ----- ----- ----- */

    @NotNull Text get(@NotNull String key, String... format) throws NullPointerException {
        synchronized (lock) {
            String str = langMap.get(key);
            if (str == null)
                throw new NullPointerException("Key has no value: " + key);
            return new Text(MessageFormat.format(str, (Object[]) format));
        }
    }

    public void load(String language) throws FileNotFoundException, NullPointerException, JsonIOException, JsonSyntaxException {
        String filename = language + ".json";
        Gson gson = new Gson();

        synchronized (lock) {
            // try to load from external file
            File file = new File(new File(DiscordSync.singleton.getDataFolder(), path), filename);
            if (file.exists() && file.isFile()) {
                langMap = gson.fromJson(
                        new FileReader(file),
                        new TypeToken<HashMap<String, String>>() {
                        }.getType()
                );
                this.language = language;
                return;
            }

            // try to load from resource
            InputStream resourceStream = DiscordSync.singleton.getResource(path + File.separator + filename);
            if (resourceStream != null) {
                langMap = gson.fromJson(
                        new InputStreamReader(resourceStream),
                        new TypeToken<HashMap<String, String>>() {
                        }.getType()
                );
                this.language = language;
                return;
            }
        }

        // not found
        throw new NullPointerException(String.format("Could not find '%s'", filename));
    }
}
