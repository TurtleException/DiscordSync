package de.eldritch.spigot.DiscordSync.user.command;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import de.eldritch.spigot.DiscordSync.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandNick implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("nick")) return false;
        if (!(sender instanceof Player player)) return true;

        User user = DiscordSync.singleton.getUserAssociationService().get(user1 -> user1.getMinecraft().getUniqueId().equals(player.getUniqueId()));
        if (user == null) {
            MessageService.sendMessage(player,
                    "user.rename.error.notVerified"
            );
            MessageService.sendMessage(player,
                    "user.verify.usage",
                    "user.verify.example"
            );
            return true;
        }

        if (args.length == 0) {
            user.setName(user.getMinecraft().getName(), false);
        } else {
            // build name
            StringBuilder name = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; i++) {
                name.append(" ").append(args[i]);
            }

            user.setName(name.toString(), true);
        }

        return true;
    }
}
