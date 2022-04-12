package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.User;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;

public class MinecraftAdvancementEvent extends MinecraftEvent {
    private final Advancement advancement;

    public MinecraftAdvancementEvent(long timestamp, @NotNull User user, Advancement advancement) {
        super(timestamp, Accessor.Channel.ADVANCEMENT, user);

        this.advancement = advancement;

        this.initBuilder();
    }

    @Override
    protected void initBuilder() {
        builder = user.newEmbed()
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
        return Text.ofGame(parseName(advancement) + "." + property).content();
    }

    private static String parseName(Advancement advancement) {
        return "advancement." + advancement.getKey().getKey().replace("/", ".");
    }
}
