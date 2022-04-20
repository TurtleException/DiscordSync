package de.eldritch.spigot.discord_sync.text;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.eldritch.spigot.discord_sync.util.IOUtil;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
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

    private final boolean messageFormat;

    public TextService(@NotNull String path, boolean messageFormat) throws JsonIOException, JsonSyntaxException, IOException {
        this.path = path;
        this.messageFormat = messageFormat;

        this.load(DEFAULT_LANGUAGE);
    }

    /* ----- ----- ----- */

    @NotNull Text get(@NotNull String key, String... format) throws NullPointerException {
        synchronized (lock) {
            String str = langMap.get(key);
            if (str == null)
                throw new NullPointerException("Key has no value: " + key);

            // Determine whether to look for "{0}" or "%s"
            String content = messageFormat
                    ? MessageFormat.format(str, (Object[]) format)
                    : str.formatted((Object[]) format);

            // translate color codes
            content = ChatColor.translateAlternateColorCodes('&', content);

            return new Text(content);
        }
    }

    public void load(String language) throws JsonIOException, JsonSyntaxException, IOException {
        Gson gson = new Gson();

        synchronized (lock) {
            langMap = gson.fromJson(
                    new FileReader(IOUtil.getFile(path + "/" + language + ".json")),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType()
            );
            this.language = language;
        }
    }
}
