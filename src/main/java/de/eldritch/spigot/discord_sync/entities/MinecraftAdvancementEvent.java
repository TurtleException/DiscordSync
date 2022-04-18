package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.LegacyUser;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;

public class MinecraftAdvancementEvent extends MinecraftEvent {
    public MinecraftAdvancementEvent(long timestamp, @NotNull LegacyUser user, Advancement advancement) {
        super(timestamp, Accessor.Channel.ADVANCEMENT, user, initBuilder(user, advancement));
    }

    private static EmbedBuilder initBuilder(LegacyUser user, Advancement advancement) {
        return user.newEmbed()
                .setDescription(Text.ofGame(
                        "chat.type.advancement.task",
                        user.getMention(),
                        "*" + getProperty(advancement, "title") + "*"
                ).content())
                .addField(
                        Text.of("events.advancement.field.description").content(),
                        Text.ofGame(getProperty(advancement, "description")).content(),
                        false
                );
    }

    private static String getProperty(Advancement advancement, String property) {
        return parseName(advancement) + "." + property;
    }

    private static String parseName(Advancement advancement) {
        return "advancements." + advancement.getKey().getKey().replace("/", ".");
    }
}
