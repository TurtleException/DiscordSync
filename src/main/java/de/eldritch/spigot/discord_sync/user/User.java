package de.eldritch.spigot.discord_sync.user;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.DiscordUtil;
import de.eldritch.spigot.discord_sync.entities.interfaces.Turtle;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class User implements Turtle {
    private final long id;

    private OfflinePlayer player;
    private Member        member;
    // TODO: implement name modification
    private String        name;

    public User(long turtle, @Nullable OfflinePlayer player, @Nullable Member member, @Nullable String name) {
        this.id     = turtle;
        this.player = player;
        this.member = member;
        this.name   = name;
    }

    public @Nullable OfflinePlayer minecraft() {
        return player;
    }

    public @Nullable Member discord() {
        return member;
    }

    public @NotNull String getEffectiveName() {
        if (name != null) return name;

        if (member != null)
            return member.getEffectiveName();

        if (player != null && player.getName() != null)
            return player.getName();

        return "???";
    }

    public @NotNull String getMention() {
        return discord() != null
                ? discord().getAsMention()
                : getEffectiveName();
    }

    public @NotNull String getEmote() {
        if (player != null && player.getName() != null) {
            final Guild guild = DiscordSync.singleton.getDiscordService().getAccessor().getGuild();
            final List<Emote> emotes = guild.getEmotesByName(player.getName(), true);

            if (emotes.size() != 0) {
                return emotes.get(0).getAsMention();
            }
        }

        return "[?]";
    }

    @Override
    public long getID() {
        return id;
    }

    /* ----- ----- ----- */

    void setMinecraft(OfflinePlayer player) {
        this.player = player;
    }

    void setDiscord(Member member) {
        this.member = member;
    }

    /* ----- ----- ----- */

    public EmbedBuilder newEmbed() {
        final String thumbnail = minecraft() != null
                ? AvatarHandler.getBustURL(minecraft())
                : null;

        return new EmbedBuilder()
                .setThumbnail(thumbnail)
                .setFooter(DiscordUtil.FOOTER_TEXT, DiscordUtil.getAvatarURL())
                .setColor(DiscordUtil.COLOR_NEUTRAL);
    }
}
