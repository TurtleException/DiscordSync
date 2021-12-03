package de.eldritch.spigot.DiscordSync.user;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class User {
    private final OfflinePlayer minecraftUser;
    private final Member        discordUser;

    private String name;


    public User(OfflinePlayer minecraftUser, Member discordUser) {
        this.minecraftUser = minecraftUser;
        this.discordUser = discordUser;
    }


    /**
     * Retrieves the nickname from Discord. If {@link Member#getNickname()} returns
     * <code>null</code> (meaning the member does not have a nickname set) this
     * method will return {@link Member#getEffectiveName()} (The {@link
     * net.dv8tion.jda.api.entities.User Users} actual name).
     */
    public String retrieveName() {
        return discordUser.getNickname() != null ? discordUser.getNickname() : discordUser.getEffectiveName();
    }

    /**
     * @return The Minecraft player
     */
    public OfflinePlayer getMinecraft() {
        return minecraftUser;
    }

    /**
     * @return The Discord member.
     */
    public Member getDiscord() {
        return discordUser;
    }

    public String getName() {
        return name;
    }

    /**
     * Changes the name of a player.
     * @param name The users new name.
     * @param updateDiscord Whether the discord nickname should be updated.
     */
    public void setName(String name, boolean updateDiscord) {
        this.name = name;

        try {
            Player player = DiscordSync.singleton.getServer().getPlayer(minecraftUser.getUniqueId());
            if (player == null) {
                throw new NullPointerException("Player '" + minecraftUser.getUniqueId() + "' is not online.");
            }

            player.setDisplayName(name);
            player.setCustomName(name);
            player.setPlayerListName(name);
        } catch (NullPointerException ignored) {}

        if (updateDiscord) {
            getDiscord().modifyNickname(name).queue();
        }
    }
}
