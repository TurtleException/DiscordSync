package de.turtle_exception.discordsync.visual;

import com.google.common.collect.Sets;
import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.SyncMessage;
import de.turtle_exception.fancyformat.formats.DiscordFormat;
import de.turtle_exception.fancyformat.formats.SpigotComponentsFormat;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

public class FormatHandler {
    private final DiscordSync plugin;

    private final String formatMinMin;
    private final String formatDisMin;
    private final String formatMinDis;
    private final String formatDisDis;

    private record MinToken(@NotNull String token,
                            @NotNull BiFunction<SyncMessage, Player, BaseComponent> func
    ) {
        public @NotNull String get() {
            return "%" + token + "%";
        }
    }

    private record DisToken(@NotNull String token,
                            @NotNull BiFunction<SyncMessage, MessageChannel, String> func
    ) {
        public @NotNull String get() {
            return "%" + token + "%";
        }
    }

    private final Set<MinToken> tokensMinMin = Sets.newConcurrentHashSet();
    private final Set<MinToken> tokensDisMin = Sets.newConcurrentHashSet();
    private final Set<DisToken> tokensMinDis = Sets.newConcurrentHashSet();
    private final Set<DisToken> tokensDisDis = Sets.newConcurrentHashSet();

    public FormatHandler(DiscordSync plugin) {
        this.plugin = plugin;

        this.formatMinMin = plugin.getConfig().getString("format.minecraftChat.minecraft");
        this.formatDisMin = plugin.getConfig().getString("format.minecraftChat.discord");
        this.formatMinDis = plugin.getConfig().getString("format.discordChat.minecraft");
        this.formatDisDis = plugin.getConfig().getString("format.discordChat.discord");

        if (formatMinMin == null || formatDisMin == null || formatMinDis == null || formatDisDis == null)
            throw new NullPointerException("config.yml is missing one or more format settings!");

        // TODO: custom user formatting
        register(new MinToken("user"   , (message, player) -> getUser(message))       , true , true );
        register(new MinToken("message", (message, player) -> getMessage(message))    , true , true );
        register(new MinToken("player" , (message, player) -> getPlayer(message))     , true , false);
        register(new MinToken("dc_nick", (message, player) -> getDiscordNick(message)), false, true );
        register(new MinToken("dc_name", (message, player) -> getDiscordName(message)), false, true );
        register(new MinToken("dc_tag" , (message, player) -> getDiscordTag(message)) , false, true );
        register(new MinToken("channel", (message, player) -> getChannel(message))    , false, true );
        register(new MinToken("guild"  , (message, player) -> getGuild(message))      , false, true );
        // TODO: mention?

        register(new DisToken("user"   , (message, channel) -> message.author().getName())                       , true , true );
        register(new DisToken("message", (message, channel) -> message.content().toString(DiscordFormat.get()))  , true , true );
        register(new DisToken("player" , (message, channel) -> message.sourceInfo().getPlayer().getDisplayName()), true , false);
        register(new DisToken("dc_nick", (message, channel) -> message.sourceInfo().getEffectiveDiscordName())   , false, true );
        register(new DisToken("dc_name", (message, channel) -> message.sourceInfo().getUser().getName())         , false, true );
        register(new DisToken("dc_tag" , (message, channel) -> message.sourceInfo().getUser().getAsTag())        , false, true );
        register(new DisToken("channel", (message, channel) -> message.sourceInfo().getChannel().getName())      , false, true );
        register(new DisToken("mention", (message, channel) -> message.sourceInfo().getUser().getAsMention())    , false, true );
        register(new DisToken("guild"  , (message, channel) -> getGuildName(message))                            , false, true );
        register(new DisToken("emote"  , (message, channel) -> {
            long target = channel instanceof GuildChannel gChannel
                    ? gChannel.getGuild().getIdLong()
                    : channel.getIdLong();
            return getEmote(message, target);
        }), true, true);
    }

    private void register(@NotNull MinToken token, boolean fromMin, boolean fromDis) {
        if (fromMin && formatMinMin.contains(token.get()))
            tokensMinMin.add(token);
        if (fromDis && formatDisMin.contains(token.get()))
            tokensDisMin.add(token);
    }

    private void register(@NotNull DisToken token, boolean fromMin, boolean fromDis) {
        if (fromMin && formatMinDis.contains(token.get()))
            tokensMinDis.add(token);
        if (fromDis && formatDisDis.contains(token.get()))
            tokensDisDis.add(token);
    }

    /* - - - */

    public @NotNull BaseComponent[] toMinecraft(@NotNull SyncMessage message, @NotNull Player recipient) {
        LinkedList<BaseComponent> components = new LinkedList<>();
        Set<MinToken> tokens;

        if (message.sourceInfo().isMinecraft()) {
            components.addAll(Arrays.asList(TextComponent.fromLegacyText(formatMinMin)));
            tokens = this.tokensMinMin;
        } else {
            components.addAll(Arrays.asList(TextComponent.fromLegacyText(formatDisMin)));
            tokens = this.tokensDisMin;
        }

        BaseComponent current;
        iterator:
        for (int i = 0; i < components.size(); i++) {
            current = components.get(i);

            for (MinToken token : tokens) {
                // skip if the current component does not contain this token
                if (!(current instanceof TextComponent tComp) || !tComp.getText().contains(token.get())) continue;

                String text = tComp.getText();
                ArrayList<BaseComponent> insertion = new ArrayList<>();

                // apply function -> split each token component into one or more new components
                BaseComponent res = token.func().apply(message, recipient);
                res.copyFormatting(tComp, false);

                // build list with all token components (replaced by res) and the intermediate TextComponents
                int index = 0;
                while (index < text.length()) {
                    int tokenIndex = text.indexOf(token.get(), index);
                    if (tokenIndex < 0) break;

                    // check for text before the token
                    if (tokenIndex > index) {
                        TextComponent duplicate = tComp.duplicate();
                        duplicate.setText(text.substring(index, tokenIndex));
                        insertion.add(duplicate);
                    }

                    // add token argument
                    insertion.add(res);

                    // increment index
                    index = tokenIndex + token.get().length();
                }

                // handle text at the end
                if (index < text.length()) {
                    TextComponent duplicate = tComp.duplicate();
                    duplicate.setText(text.substring(index));
                    insertion.add(duplicate);
                }

                // replace component(s)
                components.remove(i);
                components.addAll(i, insertion);

                // iterator should continue after the added components (-1 because i will be incremented)
                i += insertion.size() - 1;
                continue iterator;
            }
        }

        return components.toArray(new BaseComponent[]{});
    }

    public @NotNull String toDiscord(@NotNull SyncMessage message, @NotNull MessageChannel recipient) {
        Set<DisToken> tokens;
        String format;

        if (message.sourceInfo().isMinecraft()) {
            tokens = this.tokensMinDis;
            format = this.formatMinDis;
        } else {
            tokens = this.tokensDisDis;
            format = this.formatDisDis;
        }

        for (DisToken token : tokens)
            format = format.replaceAll(token.get(), token.func().apply(message, recipient));

        return format;
    }

    /* - Discord Formatting - */

    private @NotNull String getEmote(@NotNull SyncMessage message, long target) {
        return message.sourceInfo().isMinecraft()
                ? plugin.getEmoteHandler().getEmote(message.sourceInfo().getPlayer(), target)
                : plugin.getEmoteHandler().getEmote(message.sourceInfo().getChannel());
    }

    private @NotNull String getGuildName(@NotNull SyncMessage message) {
        Member member = message.sourceInfo().getMember();
        return member != null
                ? member.getGuild().getName()
                : "PRIVATE";
    }

    /* - Minecraft Formatting - */

    private @NotNull TextComponent getUser(@NotNull SyncMessage message) {
        TextComponent component = new TextComponent(message.author().getName());

        BaseComponent[] hoverText = plugin.getMessageDispatcher()
                .get("user.info", message.author().getName(), String.valueOf(message.author().getId()))
                .parse(SpigotComponentsFormat.get());

        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));

        return component;
    }

    private @NotNull TextComponent getMessage(@NotNull SyncMessage message) {
        TextComponent component = new TextComponent(message.content().parse(SpigotComponentsFormat.get()));

        BaseComponent[] hoverText = plugin.getMessageDispatcher()
                .get("chat.reference.hover", String.valueOf(message.getId()))
                .parse(SpigotComponentsFormat.get());

        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, /* TODO */ "null"));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));

        return component;
    }

    private @NotNull TextComponent getPlayer(@NotNull SyncMessage message) {
        TextComponent component = new TextComponent(message.sourceInfo().getPlayer().getDisplayName());
        Entity entity = new Entity("minecraft:player", message.sourceInfo().getPlayer().getUniqueId().toString(), null);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, entity));
        return component;
    }

    private @NotNull TextComponent getDiscordNick(@NotNull SyncMessage message) {
        TextComponent component = new TextComponent(message.sourceInfo().getEffectiveDiscordName());
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getDiscordHover(message))));
        return component;
    }

    private @NotNull TextComponent getDiscordName(@NotNull SyncMessage message) {
        TextComponent component = new TextComponent(message.sourceInfo().getUser().getName());
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getDiscordHover(message))));
        return component;
    }

    private @NotNull TextComponent getDiscordTag(@NotNull SyncMessage message) {
        TextComponent component = new TextComponent(message.sourceInfo().getUser().getAsTag());
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getDiscordHover(message))));
        return component;
    }

    private @NotNull BaseComponent[] getDiscordHover(@NotNull SyncMessage message) {
        return plugin.getMessageDispatcher()
                .get("chat.author.discord", message.sourceInfo().getUser().getAsTag(), message.sourceInfo().getUser().getId())
                .parse(SpigotComponentsFormat.get());
    }

    private @NotNull TextComponent getChannel(@NotNull SyncMessage message) {
        TextComponent component = new TextComponent(message.sourceInfo().getUser().getName());

        BaseComponent[] hoverText = plugin.getMessageDispatcher()
                .get("chat.source.discord.channel", message.sourceInfo().getChannel().getName(), message.sourceInfo().getChannel().getId())
                .parse(SpigotComponentsFormat.get());

        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));

        return component;
    }

    private @NotNull TextComponent getGuild(@NotNull SyncMessage message) {
        TextComponent component = new TextComponent(message.sourceInfo().getUser().getName());

        BaseComponent[] hoverText = plugin.getMessageDispatcher()
                .get("chat.source.discord.guild", message.sourceInfo().getMember().getGuild().getName(), message.sourceInfo().getMember().getGuild().getId())
                .parse(SpigotComponentsFormat.get());

        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));

        return component;
    }
}
