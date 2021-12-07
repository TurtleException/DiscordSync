package de.eldritch.spigot.DiscordSync.message;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class MessageService {
    private final YamlConfiguration config = new YamlConfiguration();

    private String defColor;

    private static final HoverEvent EMPTY_HOVER_EVENT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(""));
    private static final ClickEvent EMPTY_CLICK_EVENT = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "");


    public MessageService() {
        this.reloadYaml();
    }


    /**
     * Reloads the {@link YamlConfiguration} containing all messages.
     */
    public void reloadYaml() {
        try {
            config.load(new InputStreamReader(Objects.requireNonNull(DiscordSync.class.getClassLoader().getResourceAsStream("messages.yml"))));
        } catch (NullPointerException | IOException | InvalidConfigurationException e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to reload message.yml", e);
        }
        defColor = "§r" + config.getString("defaultColor", "§7");
    }

    public static void sendMessage(CommandSender commandSender, CharSequence... content) {
        ComponentBuilder builder = new ComponentBuilder(get("general.prefix", DiscordSync.singleton.getVersion().toString()));

        for (CharSequence charSequence : content) {
            LinkedList<String> args = new LinkedList<>();
            if (charSequence instanceof Container) {
                args.addAll(List.of(((Container) charSequence).getValues()));
                builder.append(DiscordSync.singleton.getMessageService().getComponent(((Container) charSequence).getKey(), args));
            } else if (charSequence instanceof String) {
                builder.append(DiscordSync.singleton.getMessageService().getComponent((String) charSequence, args));
            }
        }

        commandSender.spigot().sendMessage(builder.create());
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
     * @see MessageService#getComponent(String, LinkedList)
     */
    public static @NotNull TextComponent get(@NotNull String key, String... args) {
        LinkedList<String> argsQueue = new LinkedList<>(Arrays.asList(args));
        return DiscordSync.singleton.getMessageService().getComponent(key, argsQueue);
    }

    private @NotNull TextComponent getComponent(@NotNull String key, LinkedList<String> args) {
        String content = config.getString("messages." + key + ".content", null);

        if (content == null) {
            return new TextComponent("<key: \"" + key + "\">");
        }

        while (!args.isEmpty() && content.contains("%s")) {
            if (args.peek() == null) {
                DiscordSync.singleton.getLogger().warning("Argument '" + args.pollFirst() + "' of component '" + key + "' (\"" + content + "\") is null.");
            } else {
                content = content.replaceFirst("%s", args.pollFirst());
            }
        }

        content = defColor + content;

        content = content.replaceAll("\n", "\n§r");
        content = content.replaceAll("§r", defColor);

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(content));

        HoverEvent hoverEvent = getHoverEvent(key, args);
        component.setHoverEvent(hoverEvent != null ? hoverEvent : EMPTY_HOVER_EVENT);

        ClickEvent clickEvent = getClickEvent(key, args);
        component.setClickEvent(clickEvent != null ? clickEvent : EMPTY_CLICK_EVENT);

        return component;
    }

    private @Nullable HoverEvent getHoverEvent(@NotNull String key, @NotNull LinkedList<String> args) {
        HoverEvent.Action action;
        try {
            action = HoverEvent.Action.valueOf(config.getString("messages." + key + ".hoverEvent.action", null));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return null;
        }

        String reference = config.getString("messages." + key + ".hoverEvent.content", "<hover\\key: \"" + key + "\">");

        if (action.equals(HoverEvent.Action.SHOW_ENTITY)) {
            // TODO
            return null;
        } else if (action.equals(HoverEvent.Action.SHOW_ITEM)) {
            // TODO
            return null;
        } else if (action.equals(HoverEvent.Action.SHOW_TEXT)) {
            return new HoverEvent(action, new Text(new BaseComponent[]{this.getComponent(reference, args)}));
        }
        return null;
    }

    private @Nullable ClickEvent getClickEvent(@NotNull String key, @NotNull LinkedList<String> args) {
        ClickEvent.Action action;
        try {
            action = ClickEvent.Action.valueOf(config.getString("messages." + key + ".clickEvent.action", null));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return null;
        }

        String value = config.getString("messages." + key + ".clickEvent.content");
        if (value == null) {
            return null;
        }

        while (!args.isEmpty() && value.contains("%s")) {
            value.replaceFirst("%s", args.pollFirst());
        }

        return new ClickEvent(action, value);
    }
}
