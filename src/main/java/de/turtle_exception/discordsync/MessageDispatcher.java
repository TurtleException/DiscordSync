package de.turtle_exception.discordsync;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.turtle_exception.discordsync.util.ResourceUtil;
import de.turtle_exception.discordsync.util.StringUtil;
import de.turtle_exception.fancyformat.Format;
import de.turtle_exception.fancyformat.FormatText;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class MessageDispatcher {
    private static final String DEFAULT_LANGUAGE = "en_US";

    private final DiscordSync plugin;
    private final String language;

    private final ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();

    // cached for convenience (only has to be parsed once)
    private final BaseComponent[] prefix;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public MessageDispatcher(@NotNull DiscordSync plugin) {
        this.plugin = plugin;

        File ext = new File(plugin.getDataFolder(), "lang");
        ext.mkdir();

        // save language files
        if (!ResourceUtil.saveDefaults(plugin, "lang"))
            throw new IllegalStateException("Could not save default lang files.");

        // set language
        String lang = plugin.getConfig().getString("language");
        if (lang == null) {
            plugin.getLogger().log(Level.WARNING, "Config missing 'language' entry, defaulting to " + DEFAULT_LANGUAGE);
            lang = DEFAULT_LANGUAGE;
        }
        language = lang;


        // read JSON data
        File file = new File(ext, language + ".json");
        if (!file.exists())
            throw new NullPointerException("Language " + language + " does not exist!");
        if (!file.isFile())
            throw new IllegalArgumentException("File for language " + language + " is not a file!");

        try {
            Gson       gson   = new Gson();
            FileReader reader = new FileReader(file);
            JsonObject json   = gson.fromJson(reader, JsonObject.class);

            for (String key : json.keySet())
                this.data.put(key, json.get(key).getAsString());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Encountered an unexpected IOException while attempting to load language file.", e);
            throw new RuntimeException(e);
        } catch (UnsupportedOperationException | IllegalStateException | NullPointerException e) {
            plugin.getLogger().log(Level.SEVERE, "Encountered an unexpected Exception when attempting to read JSON lang data.", e);
            throw e;
        }

        this.prefix = ComponentSerializer.parse(get("general.prefix", plugin.getDescription().getVersion()).toString(Format.MINECRAFT_JSON));
    }

    public @NotNull FormatText get(@NotNull String key, String... format) {
        String pattern = data.get(key);

        if (pattern == null)
            throw new IllegalArgumentException("Unknown translatable key: " + key);

        return plugin.getFormatter().ofNative(StringUtil.format(pattern, format));
    }

    public @NotNull String getLanguage() {
        return this.language;
    }

    public @NotNull BaseComponent[] getPrefix() {
        return this.prefix;
    }
}
