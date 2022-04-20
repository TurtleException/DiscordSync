package de.eldritch.spigot.discord_sync.user;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.util.MiscUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.logging.Level;

public class UserUtil {
    /**
     * Attempts to provide a valid {@link OfflinePlayer} object from a String representation of its {@link UUID} and its
     * {@link Server}. If parsing the UUID fails <code>null</code> will be returned.
     * @param uuidStr String representation of the players' UUID.
     * @param server Server of the player.
     * @return OfflinePlayer with the provided UUID, possibly <code>null</code>.
     */
    public static @Nullable OfflinePlayer getPlayer(@Nullable String uuidStr, @Nullable Server server) {
        if (uuidStr == null) return null;

        UUID uuid;
        try {
            return getPlayer(UUID.fromString(uuidStr), server);
        } catch (IllegalArgumentException e) {
            MiscUtil.logUnexpectedException(Level.FINE, "when parsing \"" + uuidStr + "\" to UUID.", e);
            return null;
        }
    }

    public static @Nullable OfflinePlayer getPlayer(@Nullable UUID uuid, @Nullable Server server) {
        if (uuid == null) return null;

        if (server == null)
            server = DiscordSync.singleton.getServer();

        return server.getOfflinePlayer(uuid);
    }

    /**
     * Attempts to provide a valid {@link Member} object from a String representation of its snowflake ID and its
     * {@link Guild}. If parsing the snowflake ID fails or the {@link net.dv8tion.jda.api.entities.User User} is foreign
     * to the Guild <code>null</code> will be returned.
     * @param snowflakeStr String representation of the users' snowflake ID.
     * @param guild Guild that the user is a member of.
     * @return Member with the provided snowflake ID, possibly <code>null</code>.
     */
    public static @Nullable Member getMember(@Nullable String snowflakeStr, @Nullable Guild guild) {
        if (snowflakeStr == null) return null;

        if (guild == null)
            guild = DiscordSync.singleton.getDiscordService().getAccessor().getGuild();

        try {
            return guild.getMemberById(snowflakeStr);
        } catch (NumberFormatException e) {
            MiscUtil.logUnexpectedException(Level.FINE, "when parsing snowflake \"" + snowflakeStr + "\" to guild member.", e);
            return null;
        }
    }

    public static @Nullable Member getMember(long snowflake, @Nullable Guild guild) {
        if (guild == null)
            guild = DiscordSync.singleton.getDiscordService().getAccessor().getGuild();

        return guild.getMemberById(snowflake);
    }

    /* ----- ----- ----- */

    public static boolean isEqual(@NotNull User user1, @NotNull User user2) {
        if (user1.getID() != user2.getID()) return false;

        OfflinePlayer player1 = user1.minecraftOffline();
        OfflinePlayer player2 = user2.minecraftOffline();

        if (player1 != null && player2 != null) {
            UUID uuid1 = player1.getUniqueId();
            UUID uuid2 = player2.getUniqueId();

            if (!uuid1.equals(uuid2)) return false;
        }

        Member member1 = user1.discord();
        Member member2 = user2.discord();

        if (member1 != null && member2 != null) {
            if (member1.getIdLong() != member2.getIdLong()) return false;
        }

        return true;
    }
}
