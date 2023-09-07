package de.turtleboi.spigot.dsync;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.turtleboi.spigot.dsync.util.LangFetcher;
import de.turtleboi.spigot.dsync.util.ResourceUtil;
import de.turtleboi.spigot.dsync.util.StringUtil;
import de.turtle_exception.fancyformat.FormatText;
import de.turtle_exception.fancyformat.formats.MinecraftLegacyFormat;
import de.turtle_exception.fancyformat.formats.SpigotComponentsFormat;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageDispatcher {
    private static final String DEFAULT_LANGUAGE = "en_US";
    // TODO: this does not include snapshot versions
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+.\\d+(?:.\\d+)?");

    private final DiscordSync plugin;
    private final String language;

    private final ConcurrentHashMap<String, String> pluginData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String>   gameData = new ConcurrentHashMap<>();

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


        // load game lang data
        if (language.equalsIgnoreCase("en_US")) {
            try {
                gameData.putAll(LangFetcher.getGame());
            } catch (IOException e) {
                throw new RuntimeException("Could not load game lang data.", e);
            }
        } else {
            try {
                Matcher matcher = VERSION_PATTERN.matcher(Bukkit.getBukkitVersion());
                if (!matcher.find())
                    throw new RuntimeException("Could not parse game version from server version: " + Bukkit.getBukkitVersion());
                this.gameData.putAll(LangFetcher.get(matcher.group(), language));
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Unsupported version format: " + Bukkit.getBukkitVersion());
            } catch (IOException e) {
                throw new RuntimeException("Could not load game lang data.", e);
            }
        }


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
                this.pluginData.put(key, json.get(key).getAsString());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Encountered an unexpected IOException while attempting to load language file.", e);
            throw new RuntimeException(e);
        } catch (UnsupportedOperationException | IllegalStateException | NullPointerException e) {
            plugin.getLogger().log(Level.SEVERE, "Encountered an unexpected Exception when attempting to read JSON lang data.", e);
            throw e;
        }

        this.prefix = getPlugin("general.prefix", plugin.getDescription().getVersion()).parse(SpigotComponentsFormat.get());
    }

    public @NotNull FormatText getPlugin(@NotNull String key, String... format) {
        return plugin.getFormatter().formNative(StringUtil.format(pluginData.getOrDefault(key, key), format));
    }

    public @NotNull FormatText getGame(@NotNull String key, String... format) {
        if (pluginData.contains("overrides." + key))
            return getPlugin("overrides." + key, format);
        return plugin.getFormatter().fromFormat(gameData.getOrDefault(key, key).formatted(((Object[]) format)), MinecraftLegacyFormat.get());
    }

    public @NotNull String getLanguage() {
        return this.language;
    }

    public @NotNull BaseComponent[] getPrefix() {
        return this.prefix;
    }
}
