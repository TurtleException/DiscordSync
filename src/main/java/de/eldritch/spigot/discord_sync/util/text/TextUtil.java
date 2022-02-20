package de.eldritch.spigot.discord_sync.util.text;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.eldritch.spigot.discord_sync.DiscordSync;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.MessageFormat;
import java.util.HashMap;

public class TextUtil {
    private static final String RESOURCE_PATH = "lang";
    private static final String EXTERNAL_PATH = "lang";

    private static final String DEFAULT_LANGUAGE = "en_US";

    private static TextUtil singleton;

    private String language;
    private HashMap<String, String> langMap;

    private TextUtil() { }

    private static void checkSingleton() {
        if (singleton == null) {
            singleton = new TextUtil();
        }
    }

    private static void checkLanguage() {
        if (singleton.language == null) {
            try {
                load(DEFAULT_LANGUAGE);
            } catch (Exception e) {
                throw new IllegalStateException("Caught an exception while attempting to load default after language check failed", e);
            }
        }
    }

    /* ------------------------- */

    private @NotNull Text get0(@NotNull String key, String... format) throws NullPointerException {
        String str = langMap.get(key);
        if (str == null)
            throw new NullPointerException("Key has no value: " + key);
        return new Text(MessageFormat.format(str, (Object[]) format));
    }

    private void load0(String language) throws FileNotFoundException, NullPointerException, JsonIOException, JsonSyntaxException {
        String filename = language + ".json";
        Gson gson = new Gson();

        // try to load from external file
        File file = new File(new File(DiscordSync.singleton.getDataFolder(), EXTERNAL_PATH), filename);
        if (file.exists() && file.isFile()) {
            langMap = gson.fromJson(
                    new FileReader(file),
                    new TypeToken<HashMap<String, String>>() {}.getType()
            );
            return;
        }

        // try to load from resource
        InputStream resourceStream = DiscordSync.singleton.getResource(EXTERNAL_PATH + File.separator + filename);
        if (resourceStream != null) {
            langMap = gson.fromJson(
                    new InputStreamReader(resourceStream),
                    new TypeToken<HashMap<String, String>>(){}.getType()
            );
            return;
        }

        // not found
        throw new NullPointerException(String.format("Could not find '%s'", filename));
    }

    /* ------------------------- */

    static @NotNull Text get(@NotNull String key, String... format) throws NullPointerException {
        checkSingleton();
        checkLanguage();
        return singleton.get0(key, format);
    }

    /**
     * Loads a JSON language file by name.
     * @param language Language to load.
     * @throws IOException if a problem occurs while attempting to load the language file.
     * @throws NullPointerException if no matching language file could be found.
     * @see TextUtil#load0(String)
     */
    public static void load(String language) throws IOException, NullPointerException {
        checkSingleton();
        singleton.load0(language);
    }
}
