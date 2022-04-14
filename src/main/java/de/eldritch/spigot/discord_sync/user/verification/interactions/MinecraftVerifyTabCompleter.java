package de.eldritch.spigot.discord_sync.user.verification.interactions;

import de.eldritch.spigot.discord_sync.DiscordSync;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MinecraftVerifyTabCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) return null;

        return DiscordSync.singleton.getDiscordService().getAccessor().getGuild().getMembers().stream()
                .filter(member ->
                        member.getUser().getAsTag().toLowerCase().startsWith(args[0].toLowerCase())
                     && member.getEffectiveName().toLowerCase().startsWith(args[0].toLowerCase()))
                .map(member -> member.getUser().getAsTag())
                .toList();
    }
}
