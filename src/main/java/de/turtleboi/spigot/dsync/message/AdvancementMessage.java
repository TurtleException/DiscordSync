package de.turtleboi.spigot.dsync.message;

import de.turtleboi.spigot.dsync.DiscordSync;
import de.turtle_exception.fancyformat.formats.DiscordFormat;
import de.turtle_exception.fancyformat.formats.PlaintextFormat;
import de.turtle_exception.fancyformat.formats.SpigotComponentsFormat;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdvancementMessage extends EventMessage {
    private final Player player;
    private final Advancement advancement;
    private final String advancementKey;

    public AdvancementMessage(@NotNull DiscordSync plugin, long time, @NotNull Player player, @NotNull Advancement advancement) {
        super(plugin, time);
        this.player = player;
        this.advancement = advancement;
        this.advancementKey = "advancements." + advancement.getKey().getKey().replaceAll("/", ".");
    }

    /* - - - */

    @Override
    public @NotNull String toDiscord(@NotNull MessageChannel recipient) {
        String title            = plugin.getMessageDispatcher().getGame(advancementKey + ".title").toString(DiscordFormat.get());
        String message          = plugin.getMessageDispatcher().getGame("chat.type.advancement.task", player.getDisplayName(), "*" + title + "*").toString(DiscordFormat.get());
        String descriptionTitle = plugin.getMessageDispatcher().getPlugin("event.advancement.description").toString(DiscordFormat.get());
        String descriptionText  = plugin.getMessageDispatcher().getGame(advancementKey + ".description").toString(DiscordFormat.get());

        return message + "\n\n" + descriptionTitle + "\n" + descriptionText;
    }

    @Override
    public @NotNull BaseComponent[] toMinecraft(@NotNull Player recipient) {
        String prefix = "[";
        if (advancement.getDisplay() != null)
            prefix = "§" + advancement.getDisplay().getType().getColor().getChar() + "[";

        String          title           = plugin.getMessageDispatcher().getGame(advancementKey + ".title").toString(PlaintextFormat.get());
        BaseComponent[] message         = plugin.getMessageDispatcher().getGame("chat.type.advancement.task", player.getDisplayName(), prefix + title + "]§f").parse(SpigotComponentsFormat.get());
        BaseComponent[] descriptionText = plugin.getMessageDispatcher().getGame(advancementKey + ".description").parse(SpigotComponentsFormat.get());

        // TODO: make this text customizable (including formatting)

        TextComponent descriptionTitle = new TextComponent(title + "\n\n");
        descriptionTitle.setColor(ChatColor.GOLD);
        descriptionTitle.setBold(true);

        TextComponent description = new TextComponent(descriptionText);
        description.setColor(ChatColor.YELLOW);

        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{ descriptionTitle, description }));
        TextComponent text = new TextComponent(message);
        text.setHoverEvent(hover);
        return new BaseComponent[]{ text };
    }

    /* - - - */

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Advancement getAdvancement() {
        return advancement;
    }

    public @NotNull String getAdvancementKey() {
        return advancementKey;
    }
}
