package de.eldritch.spigot.discord_sync.user.verification;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.text.Text;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class VerificationUtil {
    public static @Nullable Member parseMember(@NotNull String input) throws NullPointerException {
        Guild guild = DiscordSync.singleton.getDiscordService().getAccessor().getGuild();


        // match snowflake
        for (Member member : guild.getMembers())
            if (member.getId().equals(input))
                return member;

        // match tag
        for (Member member : guild.getMembers())
            if (member.getUser().getAsTag().equals(input))
                return member;

        // match effective name
        for (Member member : guild.getMembers())
            if (member.getEffectiveName().equals(input))
                return member;


        return null;
    }

    public static boolean checkMember(@Nullable Member member, @NotNull Player player, String input) {
        if (member == null) {
            player.spigot().sendMessage(
                    DiscordSync.getChatPrefix(),
                    Text.of("verify.error.unknownMember", input).toBaseComponent()
            );
            return true;
        }

        return false;
    }
}
