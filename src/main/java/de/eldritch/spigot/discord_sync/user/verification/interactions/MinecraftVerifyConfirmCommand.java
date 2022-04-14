package de.eldritch.spigot.discord_sync.user.verification.interactions;

import net.dv8tion.jda.api.entities.Member;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MinecraftVerifyConfirmCommand implements CommandExecutor {
    static final ConcurrentHashMap<UUID, Member> CONFIRMATION_QUEUE = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Member member = CONFIRMATION_QUEUE.remove(player.getUniqueId());
        MinecraftVerifyCommand.openInteraction(player, member);

        return true;
    }
}
