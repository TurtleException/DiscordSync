package de.eldritch.spigot.DiscordSync.module.chat.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.chat.ChatModule;
import de.eldritch.spigot.DiscordSync.module.language.LanguageModule;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.time.Instant;

public class MinecraftEventListener implements Listener {
    private ChatModule module;

    public MinecraftEventListener(ChatModule module) {
        this.module = module;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        module.sendEmbed(this.getPlayerEmbed(event.getEntity())
                .setDescription(event.getDeathMessage())
                .build()
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        module.sendEmbed(this.getPlayerEmbed(event.getPlayer())
                .setDescription(String.format(LanguageModule.get("multiplayer.player.joined", "de_de"), event.getPlayer().getDisplayName()))
                .addField("Zuletzt online", event.getPlayer().hasPlayedBefore()
                        ? (this.getLastPlayed(event.getPlayer()))
                        : event.getPlayer().getDisplayName() + " ist das erste mal online <3", true)
                .build());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        module.sendEmbed(this.getPlayerEmbed(event.getPlayer())
                .setDescription(String.format(LanguageModule.get("multiplayer.player.left", "de_de"), event.getPlayer().getDisplayName()))
                .build()
        );
    }

    @EventHandler
    public void onPlayerAchievement(PlayerAdvancementDoneEvent event) {
        if (event.getAdvancement().getKey().getKey().startsWith("recipes")) return;

        System.out.println(event.getAdvancement().getKey().toString());

        module.sendEmbed(this.getPlayerEmbed(event.getPlayer())
                .setDescription(String.format(LanguageModule.get("chat.type.advancement.task", "de_de"), event.getPlayer().getDisplayName(), "*" + LanguageModule.getAdvancementTitle(event.getAdvancement(), "de_de") + "*"))
                .addField("Beschreibung", (LanguageModule.getAdvancementDesc(event.getAdvancement(), "de_de") != null) ? LanguageModule.getAdvancementDesc(event.getAdvancement(), "de_de") : "???", true)
                .build()
        );
    }

    private EmbedBuilder getPlayerEmbed(Player player) {
        return new EmbedBuilder()
                .setThumbnail(ChatModule.getBustUrl(player.getUniqueId().toString()))
                .setFooter(DiscordSync.class.getSimpleName(), DiscordSync.singleton.getDiscordAPI() != null
                        ? DiscordSync.singleton.getDiscordAPI().getJDA().getSelfUser().getAvatarUrl() : null)
                .setColor(0x2F3136);
    }

    private String getLastPlayed(Player player) {
        Duration duration = Duration.between(Instant.ofEpochMilli(player.getLastPlayed()), Instant.now());

        if (duration.toMillis() < Duration.ZERO.plusMinutes(1).toMillis())
            return "Vor weniger als einer Minute";
        if (duration.toMillis() < Duration.ZERO.plusMinutes(2).toMillis())
            return "Vor einer Minute";
        if (duration.toMillis() < Duration.ZERO.plusHours(2).toMillis())
            return "Vor " + duration.toMinutes() + " Minuten";
        if (duration.toMillis() < Duration.ZERO.plusDays(2).toMillis())
            return "Vor " + duration.toHours() + " Stunden";
        return "Vor " + duration.toDays() + " Tagen.";
    }
}