package de.eldritch.spigot.DiscordSync.module.emote;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Icon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String name = event.getPlayer().getName();

        DiscordSync.singleton.getServer().getScheduler().runTaskAsynchronously(DiscordSync.singleton, () -> {
            if (DiscordSync.singleton.getDiscordAPI() == null)
                return;

            // retrieve emote
            byte[] emote;
            try {
                emote = EmoteModule.retrieveAvatar(name);
            } catch (IOException e) {
                DiscordSync.singleton.getLogger().warning("Unable to retrieve emote '" + name + "'.");
                return;
            }

            // delete old emote
            for (Emote emote1 : DiscordSync.singleton.getDiscordAPI().getGuild().getEmotes())
                if (emote1.getName().equals(name)) emote1.delete().complete();

            // create new emote
            DiscordSync.singleton.getDiscordAPI().getGuild().createEmote(name, Icon.from(emote)).queue(
                    emote1 -> DiscordSync.singleton.getLogger().info("Emote '" + emote1.getName() + "' created."));
        });
    }
}