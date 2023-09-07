package de.turtleboi.spigot.dsync.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.map.LinkedMap;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LangFetcher {
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String    RESOURCE_FILE_URL = "http://resources.download.minecraft.net/%s/%s";

    public static @NotNull Map<String, String> get(@NotNull String version, @NotNull String language) throws IOException {
        Gson gson = new Gson();

        JsonObject versionManifest = gson.fromJson(loadURL(VERSION_MANIFEST_URL), JsonObject.class);
        JsonArray  versions        = versionManifest.getAsJsonArray("versions");
        JsonObject versionInfo     = null;

        for (JsonElement element : versions) {
            if (!(element instanceof JsonObject obj)) continue;

            String id = obj.get("id").getAsString();
            if (!version.equals(id)) continue;

            versionInfo = obj;
            break;
        }

        if (versionInfo == null)
            throw new IOException("Illegal version: " + version);

        String     versionURL     = versionInfo.get("url").getAsString();
        JsonObject versionPackage = gson.fromJson(loadURL(versionURL), JsonObject.class);
        JsonObject versionAssets  = versionPackage.getAsJsonObject("assetIndex");

        String     assetURL = versionAssets.get("url").getAsString();
        String     langPath = "minecraft/lang/" + language.toLowerCase() + ".json";
        JsonObject assets   = gson.fromJson(loadURL(assetURL), JsonObject.class).getAsJsonObject("objects");

        if (!assets.has(langPath))
            throw new IOException("Illegal version: " + version + "Note that 'en_US' is not supported as it is provided with the game.");

        String     hash     = assets.getAsJsonObject(langPath).get("hash").getAsString();
        String     langURL  = RESOURCE_FILE_URL.formatted(hash.substring(0, 2), hash);
        JsonObject langJson = gson.fromJson(loadURL(langURL), JsonObject.class);

        LinkedMap<String, String> map = new LinkedMap<>();
        for (String key : langJson.keySet())
            map.put(key, langJson.get(key).getAsString());
        return Map.copyOf(map);
    }

    private static @NotNull String loadURL(@NotNull String url) throws IOException {
        try (InputStream stream = new URL(url).openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();

            int c;
            while ((c = reader.read()) != -1)
                builder.append((char) c);

            return builder.toString();
        } catch (IOException e) {
            throw new IOException("Could not load data from url \"" + url + "\"");
        }
    }

    /* - - - */

    // TODO: use this in MessageDispatcher for game lang file en_US
    public static @NotNull Map<String, String> getGame() throws IOException {
        try {
            Class<?> clazz = Class.forName("net.minecraft.locale.LocaleLanguage");

            InputStream stream = clazz.getResourceAsStream("/assets/minecraft/lang/en_us.json");
            if (stream == null)
                throw new NullPointerException("Could not retrieve resource for default game lang file");

            JsonObject json = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);

            LinkedMap<String, String> map = new LinkedMap<>();
            for (String key : json.keySet())
                map.put(key, json.get(key).getAsString());
            return map;
        } catch (ClassNotFoundException | ExceptionInInitializerError e) {
            throw new IOException("Could not load class net.minecraft.locale.LocaleLanguage", e);
        }

        /*

        try {
            // get server jar
            CodeSource codeSource = Server.class.getProtectionDomain().getCodeSource();
            URI uri = codeSource.getLocation().toURI();
            File file = new File(uri);
            JarFile jarFile = new JarFile(file);

            jarFile.entries().asIterator().forEachRemaining(System.out::println);

            jarFile.close();

            FileWriter writer = new FileWriter(new File(plugin.getDataFolder(), file.getName()));
            FileReader reader = new FileReader(file);

            char[] buffer = new char[1024];
            int l;
            while ((l = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, l);
                writer.flush();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return Map.of();
         */
    }
}
