package de.eldritch.spigot.discord_sync.user;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.DiscordUtil;
import de.eldritch.spigot.discord_sync.entities.interfaces.Turtle;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.util.MiscUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public final class User implements Turtle {
    private final long turtleID;

    /**
     * The UserService responsible for this User.
     */
    private final UserService uService;

    /**
     * Verified or initial Minecraft connection.
     */
    @Nullable
    private OfflinePlayer player;
    /**
     * Verified or initial Discord connection.
     */
    @Nullable
    private Member member;

    /**
     * Custom name of this user.
     * <p>A custom name always overwrites existing names of connections. If one of the connections modified its name
     * this custom name is automatically updated and the new name will be sent to all connections.
     */
    @Nullable
    private String name;

    User(long turtleID, @NotNull UserService uService, @Nullable OfflinePlayer player, @Nullable Member member, @Nullable String name) {
        this.turtleID = turtleID;
        this.uService = uService;

        this.player = player;
        this.member = member;

        setName(name == null
                ? member != null
                    // use effective member name
                    ? member.getEffectiveName()
                    // ... otherwise use minecraft name
                    : player != null && player.getName() != null
                        ? player.getName()
                        // unknown user (no connections)
                        : "???"
                // use provided name if possible
                : name
        );
    }

    /* ----- ----- ----- */

    /**
     * Provides the {@link OfflinePlayer} that is associated with this User.
     * <p>Since the connection to Minecraft is not strictly required, this method can return <code>null</code>.
     * @return Nullable OfflinePlayer of this User.
     * @see User#minecraftOnline()
     */
    public @Nullable OfflinePlayer minecraftOffline() {
        return player;
    }

    /**
     * Attempts to provide a {@link Player} (online player) object that is associated with this User.
     * For this to be a non-null object {@link User#minecraftOffline()} has to return a non-null object and the player
     * has to be online.
     * @return Nullable Player of this User.
     * @see User#minecraftOffline()
     */
    public @Nullable Player minecraftOnline() {
        if (player == null) return null;

        return DiscordSync.singleton.getServer().getPlayer(player.getUniqueId());
    }

    /**
     * Provides the {@link Member} that is associated with this User.
     * <p>Since the connection to Discord is not strictly required, this method can return <code>null</code>.
     * @return Nullable Member of this User.
     */
    public @Nullable Member discord() {
        return member;
    }

    public void setPlayer(@Nullable OfflinePlayer newPlayer) {
        uService.getMap().updateIndexUUID(
                player    != null ? player.getUniqueId()    : null,
                newPlayer != null ? newPlayer.getUniqueId() : null,
                this
        );

        this.player = newPlayer;
    }

    public void setMember(@Nullable Member newMember) {
        uService.getMap().updateIndexSnowflake(
                member    != null ? member.getIdLong()    : null,
                newMember != null ? newMember.getIdLong() : null,
                this
        );

        this.member = newMember;
    }

    /* ----- ----- ----- */

    public @Nullable String getName() {
        return name;
    }

    /**
     * Provides the effective name of this User. This means that this method will attempt to provide the first of the
     * following strings that is not null:
     * <li> The custom name of this User.
     * <li> The result of {@link Member#getEffectiveName()} for the member object.
     * <li> The result of {@link OfflinePlayer#getName()} for the player object.
     * <li> "<code>???</code>"
     * @return Effective name of this User
     */
    public @NotNull String getEffectiveName() {
        if (name != null) return name;

        if (member != null)
            return member.getEffectiveName();

        if (player != null && player.getName() != null)
            return player.getName();

        return "???";
    }

    /**
     * Modifies the custom name of this User.
     * <p>The new name will be used as the new default for all connections of this user.
     * @param name New custom name.
     */
    public void setName(@NotNull String name) {
        this.name = name;

        // set name on Discord
        if (member != null) {
            try {
                member.modifyNickname(name).queue(null,
                        // log in case the RestAction fails
                        t -> MiscUtil.logUnexpectedException(Level.WARNING, "when attempting to modify nickname of " + member, t)
                );
            } catch (InsufficientPermissionException e) {
                DiscordSync.singleton.getLogger().log(Level.INFO, "Unable to modify nickname of member " + member.getUser().getAsTag() + " due to insufficient permissions.");
            } catch (HierarchyException e) {
                DiscordSync.singleton.getLogger().log(Level.INFO, "Unable to modify nickname of member " + member.getUser().getAsTag() + " due to hierarchy.");
            }
        }

        // set name in Minecraft
        final Player player = this.minecraftOnline();
        // name can only be changed when the player is online
        if (player != null) {
            player.setCustomName(name);
            player.setDisplayName(name);
            player.setPlayerListName(name);
        }
    }

    @Override
    public long getID() {
        return turtleID;
    }

    /* ----- UTILS ----- */

    /**
     * Attempts to provide the result of {@link Member#getAsMention()} for the member object of this user, otherwise the
     * effective name of this User.
     * @return Discord mention if possible, otherwise effective name.
     */
    public @NotNull String getMention() {
        if (member != null)
            return member.getAsMention();

        return getEffectiveName();
    }

    /**
     * Attempts to provide the name of the Discord emote for the player face of this Users player. If a valid emote
     * mention could not be provided "<code>[?]</code>" will be returned.
     * @return User emote if possible, otherwise "<code>[?]</code>".
     */
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

    /* ----- ----- ----- */

    public EmbedBuilder newEmbed() {
        final String thumbnail = player != null
                ? AvatarHandler.getBustURL(player)
                : null;

        return new EmbedBuilder()
                .setThumbnail(thumbnail)
                .setFooter(DiscordUtil.FOOTER_TEXT, DiscordUtil.getAvatarURL())
                .setColor(DiscordUtil.COLOR_NEUTRAL);
    }

    public HoverEvent newContainer() {
        return new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new net.md_5.bungee.api.chat.hover.content.Text(
                        player != null && member != null
                                ? Text.of("user.info", getEffectiveName(), String.valueOf(getID()), player.getName(), member.getUser().getAsTag()).content()
                                : Text.of("user.info.unknown", getEffectiveName(), String.valueOf(getID())).content()
                )
        );
    }
}
