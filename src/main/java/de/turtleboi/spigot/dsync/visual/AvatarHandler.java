package de.turtleboi.spigot.dsync.visual;

import de.turtleboi.spigot.dsync.DiscordSync;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class AvatarHandler {
    private final DiscordSync plugin;

    private final String avatarBust;
    private final String avatarBody;
    private final String avatarHead;

    // MINOR PERFORMANCE IMPROVEMENTS
    private final boolean bustUUID;
    private final boolean bodyUUID;
    private final boolean headUUID;
    private final boolean bustName;
    private final boolean bodyName;
    private final boolean headName;

    public AvatarHandler(@NotNull DiscordSync plugin) {
        this.plugin = plugin;

        this.avatarBust = plugin.getConfig().getString("avatar.bust", "https://mc-heads.net/avatar/%uuid%");
        this.avatarBody = plugin.getConfig().getString("avatar.body", "https://mc-heads.net/body/%uuid%");
        this.avatarHead = plugin.getConfig().getString("avatar.head", "https://minotar.net/helm/%uuid%/256");

        // make the defaults are set in the config
        plugin.getConfig().set("avatar.bust", this.avatarBust);
        plugin.getConfig().set("avatar.body", this.avatarBody);
        plugin.getConfig().set("avatar.head", this.avatarHead);

        this.bustUUID = avatarBust.contains("%uuid%");
        this.bodyUUID = avatarBody.contains("%uuid%");
        this.headUUID = avatarHead.contains("%uuid%");
        this.bustName = avatarBust.contains("%name%");
        this.bodyName = avatarBody.contains("%name%");
        this.headName = avatarHead.contains("%name%");
    }

    /* - - - */

    public @NotNull String getBust(@NotNull OfflinePlayer player) {
        String format = this.avatarBust;

        if (bustUUID)
            format = format.replaceAll("%uuid%", player.getUniqueId().toString());
        if (bustName)
            format = format.replaceAll("%name%", player.getName() != null ? player.getName() : "MHF_Question");

        return format;
    }

    public @NotNull String getBody(@NotNull OfflinePlayer player) {
        String format = this.avatarBody;

        if (bodyUUID)
            format = format.replaceAll("%uuid%", player.getUniqueId().toString());
        if (bodyName)
            format = format.replaceAll("%name%", player.getName() != null ? player.getName() : "MHF_Question");

        return format;
    }

    public @NotNull String getHead(@NotNull OfflinePlayer player) {
        String format = this.avatarHead;

        if (headUUID)
            format = format.replaceAll("%uuid%", player.getUniqueId().toString());
        if (headName)
            format = format.replaceAll("%name%", player.getName() != null ? player.getName() : "MHF_Question");

        return format;
    }
}
