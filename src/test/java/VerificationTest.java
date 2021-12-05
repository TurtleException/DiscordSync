import com.google.gson.JsonStreamParser;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class VerificationTest {
    public static void main(String[] args) throws Exception {
        System.out.println("TurtleException:    " + retrieveUUID("TurtleException"));
        System.out.println("TurtleExcep    :    " + retrieveUUID("TurtleExcep"));
        System.out.println("Miriiii:            " + retrieveUUID("Miriiii"));
        System.out.println("iFelixi:            " + retrieveUUID("iFelixi"));
        System.out.println("Foxo:               " + retrieveUUID("Foxo"));
        System.out.println("_Paddi:             " + retrieveUUID("_Paddi"));
        System.out.println("SirDrachi:          " + retrieveUUID("SirDrachi"));
        System.out.println("BluePhil:           " + retrieveUUID("BluePhil"));
        System.out.println("NichtMikki:         " + retrieveUUID("NichtMikki"));
        System.out.println("mister_Otis:        " + retrieveUUID("mister_Otis"));
        System.out.println("bySleax:            " + retrieveUUID("bySleax"));
    }


    public static @Nullable UUID retrieveUUID(String name) {
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);


            int status = con.getResponseCode();
            System.out.println(status);

            BufferedReader reader = new BufferedReader(new InputStreamReader((status < 300) ? con.getInputStream() : con.getErrorStream()));

            if (status >= 300) {
                System.out.println("Unexpected status " + status + " when retrieving UUID for user '" + name + "'.");

                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    content.append(line);
                reader.close();

                System.out.println(content);
                return null;
            }


            String str = new JsonStreamParser(reader).next().getAsJsonObject().get("id").getAsString();

            con.disconnect();

            return UUID.fromString(str.replaceFirst(
                    "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)",
                    "$1-$2-$3-$4-$5"
            ));
        } catch (Exception e) {
            System.out.println("Encountered exception when verifying user '" + name + "'.");
            e.printStackTrace();
            return null;
        }
    }
}
