package de.eldritch.spigot.discord_sync.sync;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.entities.interfaces.Referencable;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class ReferenceHelper {
    public static @Nullable Referencable getReference(net.dv8tion.jda.api.entities.Message message) {
        if (message.getMessageReference() == null)
            return null;

        net.dv8tion.jda.api.entities.Message reference = message.getMessageReference().getMessage();
        // TODO: optimize / prevent blocking
        if (reference == null)
            reference = message.getMessageReference().resolve().complete();

        return DiscordSync.singleton.getSynchronizationService().getCachedReferencableBySnowflake(reference.getIdLong());
    }

    public static @Nullable Referencable getReference(String rawMinecraftMessage) {
        String reference = parseReference(rawMinecraftMessage);

        // guard: no reference
        if (reference == null) return null;

        SynchronizationService service = DiscordSync.singleton.getSynchronizationService();

        if (reference.startsWith("@T")) {
            try {
                long snowflake = Long.parseLong(reference.substring(2));
                return service.getCachedReferencableByTurtle(snowflake);
            } catch (NumberFormatException | NullPointerException e) {
                DiscordSync.singleton.getLogger().log(Level.WARNING, "\"" + reference + "\" is not a valid turtle reference.");
                return null;
            }
        }

        if (reference.startsWith("@D")) {
            try {
                long snowflake = Long.parseLong(reference.substring(2));
                return service.getCachedReferencableBySnowflake(snowflake);
            } catch (NumberFormatException | NullPointerException e) {
                DiscordSync.singleton.getLogger().log(Level.WARNING, "\"" + reference + "\" is not a valid snowflake reference.");
                return null;
            }
        }

        return service.getCachedReferencable(reference);
    }

    private static @Nullable String parseReference(String message) {
        if (message == null)          return null;
        if (!message.startsWith("@")) return null;

        // check for direct turtle reference
        if (message.startsWith("@T")) {
            return message.substring(2, message.indexOf(" "));
        }

        // check for direct discord reference
        if (message.startsWith("@D")) {
            return message.substring(2, message.indexOf(" "));
        }

        // otherwise: parse quick reference number
        return message.substring(1, message.indexOf(" "));
    }
}
