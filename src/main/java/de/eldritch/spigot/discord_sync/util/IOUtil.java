package de.eldritch.spigot.discord_sync.util;

import de.eldritch.spigot.discord_sync.DiscordSync;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveResource(String path) throws IOException {
        InputStream stream = DiscordSync.singleton.getResource(path);

        // fail silently
        if (stream == null) return;

        File file = new File(DiscordSync.singleton.getDataFolder(), path);
        if (file.exists()) {
            // fail silently
            return;
        } else {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        stream.transferTo(new FileOutputStream(file, false));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static @NotNull File getFile(@NotNull String path) throws IOException {
        try {
            saveResource(path);
        } catch (Exception ignored) { }

        File file = new File(DiscordSync.singleton.getDataFolder(), path);

        if (file.exists()) {
            if (!file.isFile())
                throw new IOException(file + " is not a file");
        } else {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        return file;
    }
}
