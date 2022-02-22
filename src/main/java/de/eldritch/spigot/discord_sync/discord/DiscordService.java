package de.eldritch.spigot.discord_sync.discord;

import de.eldritch.spigot.discord_sync.DiscordSync;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class DiscordService {
    private final JDA jda;

    public DiscordService() throws LoginException {
        jda = initBuilder().build();
    }

    private JDABuilder initBuilder() {
        JDABuilder builder = JDABuilder.createDefault(DiscordSync.singleton.getConfig().getString("snowflake.token"));

        // TODO: configure builder

        return builder;
    }
}
