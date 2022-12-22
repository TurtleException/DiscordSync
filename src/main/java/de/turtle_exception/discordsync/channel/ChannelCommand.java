package de.turtle_exception.discordsync.channel;

import de.turtle_exception.discordsync.DiscordSync;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// TODO: subcommands (info, switch, list, create, delete, mute?)
public class ChannelCommand implements CommandExecutor, TabCompleter {
    private final DiscordSync plugin;

    public ChannelCommand(@NotNull DiscordSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            // TODO: do some fancy stuff
            player.sendMessage("You current channel is " + plugin.getChannel(player).getName());
            return true;
        }

        if (args[0].equals("reset")) {
            plugin.setChannelOverride(player.getUniqueId(), null);
            player.sendMessage("Reset channel override.");
            return true;
        }

        if (args[0].equals("list")) {
            player.sendMessage(String.join(", ", plugin.getChannelCache().stream().map(Channel::getName).toList()));
            return true;
        }

        // TODO

        return false;
    }

    // TODO
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
