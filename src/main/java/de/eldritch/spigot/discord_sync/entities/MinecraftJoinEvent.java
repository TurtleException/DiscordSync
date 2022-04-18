package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.LegacyUser;
import de.eldritch.spigot.discord_sync.util.MiscUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class MinecraftJoinEvent extends MinecraftEvent {
    public MinecraftJoinEvent(long timestamp, @NotNull LegacyUser user, long last) {
        super(timestamp, Accessor.Channel.JOIN_LEAVE, user, initBuilder(user, timestamp, last));
    }

    private static EmbedBuilder initBuilder(LegacyUser user, long timestamp, long last) {
        Text lastOnline = timestamp < last
                ? Text.of("misc.lastOnline.never", user.getMention())
                : MiscUtil.formatDuration(last, timestamp);

        return user.newEmbed()
                .setDescription(Text.ofGame("multiplayer.player.joined", user.getMention()).content())
                .addField(
                        Text.of("events.join.field.lastOnline").content(),
                        lastOnline.content(),
                        false
                );
    }
}
