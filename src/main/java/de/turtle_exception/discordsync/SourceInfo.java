package de.turtle_exception.discordsync;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SourceInfo {
    // MINECRAFT
    private final Player player;
    private final World  world;

    // DISCORD
    private final User user;
    private final Member member;
    private final MessageChannelUnion channel;

    public SourceInfo(@NotNull Player player, @NotNull World world) {
        this.player  = player;
        this.world   = world;
        this.user    = null;
        this.member  = null;
        this.channel = null;
    }

    public SourceInfo(@NotNull User user, @Nullable Member member, @NotNull MessageChannelUnion channel) {
        this.user    = user;
        this.member  = member;
        this.channel = channel;
        this.player  = null;
        this.world   = null;
    }

    /* - - - */

    public boolean isMinecraft() {
        return player != null;
    }

    public @NotNull Player getPlayer() throws IllegalStateException {
        if (player == null)
            throw new IllegalStateException("Not a Minecraft SourceInfo!");
        return player;
    }

    public @NotNull World getWorld() throws IllegalStateException {
        if (world == null)
            throw new IllegalStateException("Not a Minecraft SourceInfo!");
        return world;
    }

    public @NotNull User getUser() throws IllegalStateException {
        if (user == null)
            throw new IllegalStateException("Not a Discord SourceInfo!");
        return user;
    }

    public Member getMember() throws IllegalStateException {
        if (user == null)
            throw new IllegalStateException("Not a Discord SourceInfo!");
        return member;
    }

    public @NotNull String getEffectiveDiscordName() throws IllegalStateException {
        if (user == null)
            throw new IllegalStateException("Not a Discord SourceInfo!");
        if (member != null)
            return member.getEffectiveName();
        return user.getName();
    }

    public @NotNull MessageChannelUnion getChannel() throws IllegalStateException {
        if (channel == null)
            throw new IllegalStateException("Not a Discord SourceInfo!");
        return channel;
    }

    public boolean isFromChannel(long snowflake) {
        if (channel == null) return false;

        if (channel instanceof PrivateChannel pChannel) {
            User u = pChannel.getUser();
            return u != null && u.getIdLong() == snowflake;
        }
        return channel.getIdLong() == snowflake;
    }
}
