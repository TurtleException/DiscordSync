package de.eldritch.spigot.DiscordSync.message;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class MessageService {
    private final YamlConfiguration config = new YamlConfiguration();

    private ChatColor defColor;


    public MessageService() {
        this.reloadYaml();
    }


    /**
     * Reloads the {@link YamlConfiguration} containing all messages.
     */
    public void reloadYaml() {
        try {
            config.load(new InputStreamReader(Objects.requireNonNull(DiscordSync.class.getResourceAsStream("messages.yml"))));
            String defaultColorString = config.getString("defaultColor", "7");
            defColor = ChatColor.getByChar(defaultColorString.length() > 0 ? defaultColorString.charAt(0) : '7');
        } catch (NullPointerException | IOException | InvalidConfigurationException e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to reload message.yml", e);
        }
    }

    /**
     * Retrieves the content of a message in legacy text and returns a new
     * {@link TextComponent} containing the formatted message.
     * <p>
     *     If the key doesn't point to a message this method will return
     *     <code>null</code>.
     * </p>
     * @param key Key of the message to look for.
     * @param args Strings to replace each argument (<code>%s</code>) in the
     *             message.
     * @return Formatted {@link TextComponent}.
     *
     * @see MessageService#get(String, LinkedList)
     */
    public @NotNull TextComponent get(@NotNull String key, String... args) {
        LinkedList<String> argsQueue = new LinkedList<>(List.of(args));
        return this.get(key, argsQueue);
    }

    private @NotNull TextComponent get(@NotNull String key, LinkedList<String> args) {
        String content = config.getString("messages." + key + ".content", null);

        if (content == null) {
            return new TextComponent("<key: \"" + key + "\">");
        }

        while (!args.isEmpty() && content.contains("%s")) {
            content.replaceFirst("%s", args.pollFirst());
        }

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(content));
        component.setColor(defColor);

        HoverEvent hoverEvent = getHoverEvent(key, args);
        if (hoverEvent != null) {
            component.setHoverEvent(hoverEvent);
        }

        ClickEvent clickEvent = getClickEvent(key, args);
        if (clickEvent != null) {
            component.setClickEvent(clickEvent);
        }

        return component;
    }

    private @Nullable HoverEvent getHoverEvent(@NotNull String key, @NotNull LinkedList<String> args) {
        HoverEvent.Action action;
        try {
            action = HoverEvent.Action.valueOf(config.getString("messages." + key + ".hoverEvent.action", null));
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        if (action.equals(HoverEvent.Action.SHOW_ENTITY)) {
            // TODO
            return null;
        } else if (action.equals(HoverEvent.Action.SHOW_ITEM)) {
            // TODO
            return null;
        } else if (action.equals(HoverEvent.Action.SHOW_TEXT)) {
            return new HoverEvent(action, new Text(new BaseComponent[]{this.get(key + ".hoverEvent", args)}));
        }
        return null;
    }

    private @Nullable ClickEvent getClickEvent(@NotNull String key, @NotNull LinkedList<String> args) {
        ClickEvent.Action action;
        try {
            action = ClickEvent.Action.valueOf(config.getString("messages." + key + ".clickEvent.action", null));
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        String value = config.getString("messages." + key + ".click.content");
        if (value == null) {
            return null;
        }

        while (!args.isEmpty() && value.contains("%s")) {
            value.replaceFirst("%s", args.pollFirst());
        }

        return new ClickEvent(action, value);
    }
}
