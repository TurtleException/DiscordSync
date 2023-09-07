package de.turtleboi.spigot.dsync.util;

import de.turtleboi.spigot.dsync.DiscordSync;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Stream;

public class ResourceUtil {
    private ResourceUtil() { }

    public static boolean saveDefault(@NotNull DiscordSync plugin, @NotNull String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (file.exists()) return false;

        InputStream stream = plugin.getResource(path);
        if (stream == null) {
            plugin.getLogger().log(Level.WARNING, "Could not save resource " + path + " because it does not exist.");
            return false;
        }

        try {
            Files.copy(stream, file.toPath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save resource " + path + " due to an IOException.", e);
            return false;
        }
        return true;
    }

    public static boolean saveDefaults(@NotNull DiscordSync plugin, @NotNull String path) {
        URL url = plugin.getPluginClassLoader().getResource(path);
        if (url == null) {
            plugin.getLogger().log(Level.WARNING, "Could not save resources " + path + " because the URL for that path is null.");
            return false;
        }

        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save resources " + path + " due to a URISyntaxException.", e);
            return false;
        }

        try (FileSystem fs = FileSystems.newFileSystem(uri, Map.of())) {
            Path source = fs.getPath("/" + path);
            Path dest   = Paths.get(plugin.getDataFolder().getPath(), path);

            try (Stream<Path> stream = Files.walk(source)) {
                stream.forEach(srcPath -> {
                    if (srcPath.equals(source))
                        return;

                    try {
                        Path absoluteSrcPath = Paths.get(srcPath.toString());
                        Path        destPath = dest.resolve(absoluteSrcPath.getFileName());

                        if (Files.exists(destPath)) return;

                        if (Files.isDirectory(srcPath)) {
                            Files.createDirectory(destPath);
                        } else {
                            Files.copy(srcPath, destPath);
                        }
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.WARNING, "Could not save resource " + srcPath + " due to an IOException.", e);
                    }
                });
            }

            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save resources " + path + " due to an IOException.", e);
            return false;
        }
    }
}
