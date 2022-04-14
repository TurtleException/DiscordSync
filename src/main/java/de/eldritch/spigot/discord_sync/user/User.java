package de.eldritch.spigot.discord_sync.user;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.DiscordUtil;
import de.eldritch.spigot.discord_sync.entities.interfaces.Turtle;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class User implements Turtle {
    private final long id;

    private OfflinePlayer player;
    private Member        member;
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

    public @Nullable String getName() {
        return name;
    }

    public @NotNull String getMention() {
        return discord() != null
                ? discord().getAsMention()
                : (getName() != null ? getName() : "???");
    }

    public @Nullable String getEmote() {
        return player != null ? player.getName() : null;
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
