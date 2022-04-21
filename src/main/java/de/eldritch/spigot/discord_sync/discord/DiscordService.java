package de.eldritch.spigot.discord_sync.discord;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.sync.listener.DiscordChatListener;
import de.eldritch.spigot.discord_sync.user.listener.DiscordNameListener;
import de.eldritch.spigot.discord_sync.util.MiscUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordService {
    /**
     * Blueprint to build the {@link JDA} instance in the Constructor.
     */
    private static final JDABuilder BUILDER = JDABuilder
            .create(
                    // for now simply enable all intents for simplicity
                    EnumSet.allOf(GatewayIntent.class)
            )
            .setStatus(OnlineStatus.IDLE)
            .setActivity(Activity.playing("Minecraft"))
            .addEventListeners(
                    new DiscordChatListener(),
                    new DiscordNameListener()
            );

    /**
     * JDA instance
     * @see DiscordService#BUILDER
     */
    private final JDA jda;
    /**
     * Utility to access channels easier.
     */
    private final Accessor accessor;

    public DiscordService() throws LoginException, InterruptedException, NumberFormatException, NullPointerException {
        jda = BUILDER
                .setToken(DiscordSync.singleton.getConfig().getString("discord.token"))
                .build();

        DiscordSync.singleton.getLogger().log(Level.INFO, "Awaiting JDA availability...");
        jda.awaitReady();
        DiscordSync.singleton.getLogger().log(Level.INFO, "JDA is ready!");

        try {
            accessor = new Accessor(this);
        } catch (NumberFormatException | NullPointerException ex) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, DiscordUtil.LOG_ACCESSOR_EXCEPTION);
            throw ex;
        }
    }

    /**
     * Attempts to shut down the JDA instance and awaits termination.
     */
    public void shutdown() {
        Logger logger = DiscordSync.singleton.getLogger();

        logger.log(Level.FINE, "Shutting down JDA...");

        jda.shutdown();
        try {
            MiscUtil.await(() -> jda.getStatus() == JDA.Status.SHUTDOWN, 10, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException e) {
            logger.log(Level.WARNING, "Failed to await status.");
        }

        if (jda.getStatus() != JDA.Status.SHUTDOWN) {
            logger.log(Level.INFO, "Attempting to force shutdown...");
            jda.shutdownNow();

            try {
                MiscUtil.await(() -> jda.getStatus() == JDA.Status.SHUTDOWN, 2, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException e) {
                logger.log(Level.WARNING, "Failed to await status.");
            } finally {
                logger.log(Level.INFO, "JDA is now shut down.");
            }
        } else {
            logger.log(Level.INFO, "JDA is now shut down");
        }
    }

    public Accessor getAccessor() {
        return accessor;
    }

    public JDA getJDA() {
        return jda;
    }
}
