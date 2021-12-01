package de.eldritch.spigot.DiscordSync.module.name;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class UserWrapper {
    private final UUID minecraft;
    private final long discord;

    private String name;

    public UserWrapper(@NotNull UUID minecraft, long discord, @NotNull String name) {
        this.minecraft = minecraft;
        this.discord   = discord;
        this.name      = name;
    }

    public UserWrapper(UUID minecraft, long discord) {
        this.minecraft = minecraft;
        this.discord = discord;

        this.name = this.retrieveName();
    }

    public @NotNull UUID getMinecraft() {
        return minecraft;
    }

    public long getDiscord() {
        return discord;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String retrieveName() {
        try {
            return Objects.requireNonNull(Objects.requireNonNull(DiscordSync.singleton.getDiscordAPI().getGuild().getMemberById(discord)).getNickname());
        } catch (NullPointerException e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to retrieve name from member " + discord + ".");
            return "null";
        }
    }
}
