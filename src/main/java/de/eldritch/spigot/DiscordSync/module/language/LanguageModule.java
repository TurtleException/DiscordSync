package de.eldritch.spigot.DiscordSync.module.language;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.advancement.Advancement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageModule extends PluginModule {
    private static LanguageModule singleton;

    private HashMap<String, HashMap<String, String>> LANG;

    @Override
    public void onEnable() throws PluginModuleEnableException {
        singleton = this;

        LANG = new HashMap<>();
        this.indexLangMap();
    }

    private void indexLangMap() {
        try {
            for (File langFile : Objects.requireNonNull(new File(DiscordSync.singleton.getDataFolder(), "lang").listFiles(File::isFile))) {
                HashMap<String, String> langMap = new HashMap<>();

                for (String line : Files.readAllLines(langFile.toPath())) {
                    Matcher matcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\",?", Pattern.CASE_INSENSITIVE).matcher(line);
                    if (matcher.find())
                        langMap.put(matcher.group(1), matcher.group(2));
                }

                LANG.put(langFile.getName().replaceFirst("[.][^.]+$", ""), langMap);
            }
        } catch (NullPointerException | SecurityException | IOException ignored) {}
    }

    public String getLanguage() {
        return getConfig().getString("language.default", "default");
    }

    public String getString(String key, String language) {
        return StringEscapeUtils.unescapeJava(LANG
                .getOrDefault(language, null)
                .getOrDefault(key, null));
    }

    public static String get(String key, String language) {
        return singleton.getString(key, language);
    }

    public static String getAdvancementTitle(Advancement adv, String language) {
        return get("advancements." + adv.getKey().getKey().replace('/', '.') + ".title", language);
    }

    public static String getAdvancementDesc(Advancement adv, String language) {
        return get("advancements." + adv.getKey().getKey().replace('/', '.') + ".description", language);
    }
}