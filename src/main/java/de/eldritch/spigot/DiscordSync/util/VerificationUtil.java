package de.eldritch.spigot.DiscordSync.util;

import com.google.gson.JsonStreamParser;
import de.eldritch.spigot.DiscordSync.DiscordSync;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;

public class VerificationUtil {
    public static @Nullable UUID retrieveUUID(String name) {
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);


            int status = con.getResponseCode();

            BufferedReader reader = new BufferedReader(new InputStreamReader((status < 300) ? con.getInputStream() : con.getErrorStream()));

            if (status != 200) {
                DiscordSync.singleton.getLogger().warning("Unexpected status " + status + " when retrieving UUID for user '" + name + "'.");

                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    content.append(line);
                reader.close();

                DiscordSync.singleton.getLogger().warning(content.toString());
                return null;
            }


            String str = new JsonStreamParser(reader).next().getAsJsonObject().get("id").getAsString();

            con.disconnect();

            return UUID.fromString(str.replaceFirst(
                    "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)",
                    "$1-$2-$3-$4-$5"
            ));
        } catch (Exception e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Encountered exception when verifying user '" + name + "'.", e);
            return null;
        }
    }
}
