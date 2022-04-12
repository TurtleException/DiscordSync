package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.User;
import de.eldritch.spigot.discord_sync.util.MiscUtil;
import org.jetbrains.annotations.NotNull;

public class MinecraftJoinEvent extends MinecraftEvent {
    private final long last;

    public MinecraftJoinEvent(long timestamp, @NotNull User user, long last) {
        super(timestamp, Accessor.Channel.JOIN_LEAVE, user);

        this.last = last;

        this.initBuilder();
    }

    @Override
    protected void initBuilder() {
        Text lastOnline = timestamp < last
                ? Text.of("misc.lastOnline.never", user.getName())
                : MiscUtil.formatDuration(last, timestamp);

        builder = user.newEmbed()
                .setDescription(Text.ofGame("multiplayer.player.joined", user.getName()).content())
                .addField(
                        Text.of("events.join.field.lastOnline").content(),
                        lastOnline.content(),
                        false
                );
    }
}
