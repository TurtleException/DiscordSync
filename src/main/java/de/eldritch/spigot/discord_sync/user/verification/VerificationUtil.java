package de.eldritch.spigot.discord_sync.user.verification;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.verification.interactions.DiscordButtonListener;
import de.eldritch.spigot.discord_sync.user.verification.interactions.MinecraftVerifyCommand;
import de.eldritch.spigot.discord_sync.user.verification.interactions.MinecraftVerifyConfirmCommand;
import de.eldritch.spigot.discord_sync.user.verification.interactions.MinecraftVerifyTabCompleter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class VerificationUtil {
    public static void initCommands() throws NullPointerException {
        PluginCommand commandVerify  = DiscordSync.singleton.getCommand("verify");
        PluginCommand commandConfirm = DiscordSync.singleton.getCommand("verify-confirm");

        if (commandVerify == null)
            throw new NullPointerException("Could not assign CommandExecutor to command \"verify\" because the command does not exist.");

        if (commandConfirm == null)
            throw new NullPointerException("Could not assign CommandExecutor to command \"verify-confirm\" because the command does not exist.");

        commandVerify.setExecutor(new MinecraftVerifyCommand());
        commandVerify.setTabCompleter(new MinecraftVerifyTabCompleter());

        commandConfirm.setExecutor(new MinecraftVerifyConfirmCommand());
    }

    public static void initListener() {
        DiscordSync.singleton.getDiscordService().getJDA().getEventManager().register(new DiscordButtonListener());
    }

    /* ----- ----- ----- */

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
