package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.User;
import org.jetbrains.annotations.NotNull;

public class MinecraftQuitEvent extends MinecraftEvent {
    public MinecraftQuitEvent(long timestamp, @NotNull User user) {
        super(timestamp, Accessor.Channel.JOIN_LEAVE, user);

        this.initBuilder();
    }

    @Override
    protected void initBuilder() {
        builder = user.newEmbed()
                .setDescription(Text.ofGame("multiplayer.player.left", user.getName()).content());
    }
}
