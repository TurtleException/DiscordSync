package de.turtleboi.spigot.dsync.core.chat;

import de.turtleboi.fancyformat.Format;
import de.turtleboi.fancyformat.FormatText;
import de.turtleboi.fancyformat.format.SpigotComponentFormat;
import de.turtleboi.spigot.dsync.api.chat.MessageHandler;
import de.turtleboi.spigot.dsync.api.entity.Message;
import de.turtleboi.spigot.dsync.core.DiscordSyncCore;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MessageDispatcher implements MessageHandler {
    private final DiscordSyncCore plugin;

    private final String formatMinecraft;
    private final String formatDiscord;

    private final Format<BaseComponent> format = new SpigotComponentFormat();

    public MessageDispatcher(@NotNull DiscordSyncCore plugin) {
        this.plugin = plugin;

        this.formatMinecraft = this.plugin.getConfig().getString("format.minecraft", "§6%user%§8:  §7%message%");
        this.formatDiscord   = this.plugin.getConfig().getString("format.discord"  , "§9%user%§8:  §7%message%");
    }

    @Override
    public void onMessage(@NotNull Message message) {
        FormatText content = message.getContent();
        BaseComponent contentComponent = content.toFormat(this.format);

        String format = null;

        String source = message.getContext("source");
        if ("minecraft".equals(source)) {
            format = this.formatMinecraft;

            format = format.replaceAll("%world%", message.getContext("world.name"));
            format = format.replaceAll("%player%", message.getContext("author.minecraft.name"));
            format = format.replaceAll("%uuid%", message.getContext("author.minecraft.uuid").toString());
        }
        if ("discord".equals(source)) {
            format = this.formatDiscord;

            format = format.replaceAll("%snowflake%", message.getContext("author.discord.snowflake"));
            format = format.replaceAll("%username%", message.getContext("author.discord.username"));
        }

        if (format == null)
            throw new IllegalArgumentException("Unknown source: " + source);

        format = format.replaceAll("%user%", message.getAuthor().getName());

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(format));
        injectContent(component, contentComponent);

        this.plugin.getServer().spigot().broadcast(component);
    }

    private static void injectContent(@NotNull BaseComponent component, @NotNull BaseComponent content) {
        if (component instanceof TextComponent tComponent) {
            if (tComponent.getText().equals("%message%")) {
                tComponent.setText(null);

                List<BaseComponent> oldExtra = tComponent.getExtra();
                if (oldExtra == null)
                    oldExtra = List.of();

                List<BaseComponent> newExtra = new ArrayList<>(oldExtra.size() + 1);

                newExtra.add(content);
                newExtra.addAll(oldExtra);

                tComponent.setExtra(newExtra);
            }
        }

        List<BaseComponent> extra = component.getExtra();
        if (extra != null)
            for (BaseComponent child : extra)
                injectContent(child, content);
    }
}
