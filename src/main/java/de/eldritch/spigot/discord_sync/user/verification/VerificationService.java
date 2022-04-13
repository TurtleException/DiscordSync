package de.eldritch.spigot.discord_sync.user.verification;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.user.verification.interactions.DiscordButtonListener;
import de.eldritch.spigot.discord_sync.user.verification.interactions.MinecraftVerifyCommand;
import de.eldritch.spigot.discord_sync.user.verification.interactions.MinecraftVerifyTabCompleter;
import org.bukkit.command.PluginCommand;

public class VerificationService {
    public VerificationService() throws NullPointerException {
        this.initCommand();
        this.initListener();
    }

    private void initCommand() throws NullPointerException {
        PluginCommand command = DiscordSync.singleton.getCommand("verify");

        if (command == null)
            throw new NullPointerException("Could not assign CommandExecutor to command \"verify\" because the command does not exist.");

        command.setExecutor(new MinecraftVerifyCommand());
        command.setTabCompleter(new MinecraftVerifyTabCompleter());
    }

    private void initListener() {
        DiscordSync.singleton.getDiscordService().getJDA().getEventManager().register(new DiscordButtonListener());
    }
}
