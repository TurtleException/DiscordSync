package de.eldritch.spigot.discord_sync.user.avatar;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.util.MiscUtil;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Icon;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;

public class AvatarHandler {
    // TODO: use internal webserver to avoid dependency on third-party API
    private static final String MINECRAFT_AVATAR_BUST = "https://mc-heads.net/avatar/%s";
    private static final String MINECRAFT_AVATAR_HEAD = "https://minotar.net/helm/%s/256";

    public void loadEmote(@NotNull OfflinePlayer player) {
        if (player.getName() == null) return;

        DiscordSync.singleton.getServer().getScheduler().runTaskAsynchronously(DiscordSync.singleton, () -> {
            try {
                boolean updated = false;

                // retrieve emote
                URL url = new URL(MINECRAFT_AVATAR_HEAD.formatted(player.getUniqueId()));
                byte[] image = MiscUtil.retrieveByteArrayFromURL(url);

                // delete old emote
                for (Emote emote : DiscordSync.singleton.getDiscordService().getAccessor().getGuild().getEmotes()) {
                    if (emote.getName().equals(player.getName())) {
                        emote.delete().complete();
                        updated = true;
                    }
                }

                // create new emote
                final String suffix = updated ? "updated." : "created.";
                DiscordSync.singleton.getDiscordService().getAccessor().getGuild().createEmote(player.getName(), Icon.from(image)).queue(
                        emote -> {
                            DiscordSync.singleton.getLogger().log(
                                    Level.INFO,
                                    "Emote \"" + player.getName() + "\" " + suffix
                            );
                        },
                        throwable -> {
                            DiscordSync.singleton.getLogger().log(
                                    Level.WARNING,
                                    "Emote \"" + player.getName() + "\" could not be " + suffix
                            );
                        }
                );
            } catch (IOException e) {
                DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to handle emote " + player.getUniqueId(), e);
            }
        });
    }

    public String getBustURL(UUID uuid) {
        return MINECRAFT_AVATAR_BUST.formatted(uuid.toString());
    }
}
