package de.eldritch.spigot.DiscordSync.module.chat.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.chat.ChatModule;
import de.eldritch.spigot.DiscordSync.module.language.LanguageModule;
import de.eldritch.spigot.DiscordSync.user.User;
import de.eldritch.spigot.DiscordSync.util.DiscordUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

public class MinecraftEventListener implements Listener {
    private ChatModule module;

    public MinecraftEventListener(ChatModule module) {
        this.module = module;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        User user = DiscordSync.singleton.getUserAssociationService().get(user1 -> user1.getMinecraft().getUniqueId().equals(event.getEntity().getUniqueId()));

        module.sendEmbed(this.getPlayerEmbed(event.getEntity())
                .setDescription(formatMessage(event.getDeathMessage(), event.getEntity(), user))
                .build()
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user = DiscordSync.singleton.getUserAssociationService().get(user1 -> user1.getMinecraft().getUniqueId().equals(event.getPlayer().getUniqueId()));
        if (user != null) {
            // update name in minecraft
            user.setName(user.getName(), false);

            if (event.getJoinMessage() != null) {
                event.setJoinMessage(event.getJoinMessage().replaceFirst(Pattern.quote(event.getPlayer().getName()), user.getName()));
            }
        }

        module.sendEmbed(this.getPlayerEmbed(event.getPlayer())
                .setDescription(formatMessage(String.format(LanguageModule.get("multiplayer.player.joined", "de_de"), event.getPlayer().getDisplayName()), event.getPlayer(), user))
                .addField("Zuletzt online", event.getPlayer().hasPlayedBefore()
                        ? (this.getLastPlayed(event.getPlayer()))
                        : event.getPlayer().getDisplayName() + " ist das erste mal online <3", true)
                .build());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        User user = DiscordSync.singleton.getUserAssociationService().get(user1 -> user1.getMinecraft().getUniqueId().equals(event.getPlayer().getUniqueId()));

        if (user != null && event.getQuitMessage() != null) {
            event.setQuitMessage(event.getQuitMessage().replaceFirst(Pattern.quote(event.getPlayer().getName()), user.getName()));
        }

        module.sendEmbed(this.getPlayerEmbed(event.getPlayer())
                .setDescription(formatMessage(String.format(LanguageModule.get("multiplayer.player.left", "de_de"), event.getPlayer().getDisplayName()), event.getPlayer(), user))
                .build()
        );
    }

    @EventHandler
    public void onPlayerAchievement(PlayerAdvancementDoneEvent event) {
        User user = DiscordSync.singleton.getUserAssociationService().get(user1 -> user1.getMinecraft().getUniqueId().equals(event.getPlayer().getUniqueId()));

        if (event.getAdvancement().getKey().getKey().startsWith("recipes")) return;

        module.sendEmbed(this.getPlayerEmbed(event.getPlayer())
                .setDescription(formatMessage(String.format(LanguageModule.get("chat.type.advancement.task", "de_de"), event.getPlayer().getDisplayName(), "*" + LanguageModule.getAdvancementTitle(event.getAdvancement(), "de_de") + "*"), event.getPlayer(), user))
                .addField("Beschreibung", (LanguageModule.getAdvancementDesc(event.getAdvancement(), "de_de") != null) ? LanguageModule.getAdvancementDesc(event.getAdvancement(), "de_de") : "???", true)
                .build()
        );
    }

    private EmbedBuilder getPlayerEmbed(Player player) {
        return new EmbedBuilder()
                .setThumbnail(ChatModule.getBustUrl(player.getUniqueId().toString()))
                .setFooter(DiscordUtil.FOOTER_TEXT, DiscordUtil.getAvatarURL())
                .setColor(DiscordUtil.COLOR_NEUTRAL);
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

    public String formatMessage(String str, Player player, User user) {
        if (str != null && user != null) {
            str = str.replaceFirst(Pattern.quote(player != null ? player.getDisplayName() : user.getName()), user.getDiscord().getAsMention());
        }

        return str;
    }
}