package de.turtleboi.spigot.dsync.core.chat;

import de.turtleboi.fancyformat.Format;
import de.turtleboi.fancyformat.FormatText;
import de.turtleboi.fancyformat.format.SpigotComponentFormat;
import de.turtleboi.spigot.dsync.api.chat.MessageHandler;
import de.turtleboi.spigot.dsync.api.entity.Message;
import de.turtleboi.spigot.dsync.core.DiscordSyncCore;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
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

        this.formatMinecraft = this.plugin.getConfig().getString("format.minecraft", "§6%user%§8: %reply% §7%message%");
        this.formatDiscord   = this.plugin.getConfig().getString("format.discord"  , "§9%user%§8: %reply% §7%message%");
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

        BaseComponent component = new TextComponent(TextComponent.fromLegacyText(format));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + message.getId() + " "));
        component = injectContent("%message%", component, contentComponent);

        BaseComponent replyToComponent = this.buildReplyTag(message);
        component = injectContent("%reply%", component, replyToComponent);

        sanitizeNullText(component);

        this.plugin.getServer().spigot().broadcast(component);
    }

    private static void sanitizeNullText(@NotNull BaseComponent component) {
        if (component instanceof TextComponent tComponent) {
            if (tComponent.getText() == null)
                tComponent.setText("");
        }

        List<BaseComponent> extra = component.getExtra();
        if (extra != null)
            for (BaseComponent baseComponent : extra)
                sanitizeNullText(baseComponent);
    }

    private static BaseComponent injectContent(@NotNull String token, final @NotNull BaseComponent component, final @NotNull BaseComponent content) {
        BaseComponent c = component;

        if (component instanceof TextComponent tComponent)
            c = splitAndInjectComponents(token, tComponent, content);

        List<BaseComponent> extra = component.getExtra();
        if (extra != null) {
            // unconventional but handy iterator (method will return the initial element most of the time)
            extra.replaceAll(baseComponent -> injectContent(token, baseComponent, content));
        }

        return c;
    }

    private static TextComponent splitAndInjectComponents(@NotNull String token, @NotNull TextComponent component, @NotNull BaseComponent content) {
        String text = component.getText();
        if (text == null)
            return component;

        if (!text.contains(token))
            return component;

        int tokenIndex = text.indexOf(token);

        if (tokenIndex == -1)
            return component;

        ArrayList<BaseComponent> components = new ArrayList<>();

        String beforeToken = text.substring(0, tokenIndex);
        String  afterToken = text.substring(tokenIndex + token.length());

        if (!beforeToken.isEmpty()) {
            TextComponent beforeComponent = new TextComponent();
            beforeComponent.setText(beforeToken);

            copyFormatting(component, beforeComponent, true);

            components.add(beforeComponent);
        }

        BaseComponent contentDup = content.duplicate();
        copyFormatting(component, contentDup, false);
        components.add(contentDup);

        List<BaseComponent> extra = component.getExtra();
        if (!afterToken.isEmpty() || (extra != null && !extra.isEmpty())) {
            TextComponent afterComponent = new TextComponent();
            afterComponent.setText(afterToken);

            copyFormatting(component, afterComponent, true);

            if (extra != null)
                afterComponent.setExtra(extra);

            components.add(afterComponent);
        }

        return new TextComponent(components.toArray(BaseComponent[]::new));
    }

    private TextComponent buildReplyTag(@NotNull Message message) {
        Message referencedMessage = message.getReplyTo();

        if (referencedMessage == null)
            return new TextComponent();

        TextComponent component = new TextComponent("[REPLY]");

        List<BaseComponent> components = new ArrayList<>();

        TextComponent c1 = new TextComponent();
        c1.setColor(ChatColor.GOLD);
        c1.setText("Replied to ");
        components.add(c1);

        TextComponent c2 = new TextComponent();
        c2.setText(referencedMessage.getAuthor().getName());
        c2.setColor(ChatColor.GOLD);
        c2.setBold(true);
        String source = message.getContext("source");
        if ("discord".equals(source))
            c2.setColor(ChatColor.DARK_BLUE);
        components.add(c2);

        TextComponent c3 = new TextComponent();
        c3.setText("\n\n");
        components.add(c3);

        BaseComponent refContent = referencedMessage.getContent().toFormat(this.format);
        refContent.setColor(ChatColor.GRAY);
        components.add(refContent);

        TextComponent c4 = new TextComponent();
        c4.setText("\n\n");
        components.add(c4);

        TextComponent c5 = new TextComponent();
        c5.setText("@" + referencedMessage.getId());
        c5.setColor(ChatColor.DARK_GRAY);
        components.add(c5);

        Text hoverText = new Text(components.toArray(BaseComponent[]::new));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

        return component;
    }

    private static void copyFormatting(BaseComponent from, BaseComponent to, boolean overwriteNulls) {
        if (to.getColorRaw() == null || overwriteNulls)
            to.setColor(from.getColorRaw());

        if (to.isBoldRaw() == null || overwriteNulls)
            to.setBold(from.isBoldRaw());
        if (to.isItalicRaw() == null || overwriteNulls)
            to.setItalic(from.isItalicRaw());
        if (to.isUnderlinedRaw() == null || overwriteNulls)
            to.setUnderlined(from.isUnderlinedRaw());
        if (to.isStrikethroughRaw() == null || overwriteNulls)
            to.setStrikethrough(from.isStrikethroughRaw());
        if (to.isObfuscatedRaw() == null || overwriteNulls)
            to.setObfuscated(from.isObfuscatedRaw());

        if (to.getHoverEvent() == null || overwriteNulls)
            to.setHoverEvent(from.getHoverEvent());
        if (to.getClickEvent() == null || overwriteNulls)
            to.setClickEvent(from.getClickEvent());
    }
}
