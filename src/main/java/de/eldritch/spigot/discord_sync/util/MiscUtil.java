package de.eldritch.spigot.discord_sync.util;

import de.eldritch.spigot.discord_sync.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

public class MiscUtil {
    @SuppressWarnings("BusyWait")
    public static void await(@NotNull BooleanSupplier condition, int timeout, @NotNull TimeUnit unit) throws TimeoutException, InterruptedException {
        final long timeoutMillis = System.currentTimeMillis() + unit.toMillis(timeout);

        while (!condition.getAsBoolean()) {
            if (timeoutMillis >= System.currentTimeMillis())
                throw new TimeoutException("Timed out");
            Thread.sleep(20L);
        }
    }

    public static byte[] retrieveByteArrayFromURL(URL url) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (InputStream in = url.openStream()) {
            int n;
            byte[] buffer = new byte[1024];

            while (-1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
            }
        }

        return out.toByteArray();
    }

    public static Text formatDuration(long from, long to) {
        return formatDuration(Duration.between(Instant.ofEpochMilli(from), Instant.ofEpochMilli(to)));
    }

    public static Text formatDuration(Duration duration) {
        if (duration.toMillis() < Duration.ZERO.plusMinutes(1).toMillis())
            return Text.of("misc.lastOnline.immediate");
        if (duration.toMillis() < Duration.ZERO.plusMinutes(2).toMillis())
            return Text.of("misc.lastOnline.minute");
        if (duration.toMillis() < Duration.ZERO.plusHours(2).toMillis())
            return Text.of("misc.lastOnline.minutes", String.valueOf(duration.toMinutes()));
        if (duration.toMillis() < Duration.ZERO.plusDays(2).toMillis())
            return Text.of("misc.lastOnline.hours", String.valueOf(duration.toHours()));
        return Text.of("misc.lastOnline.days", String.valueOf(duration.toDays()));
    }
}
