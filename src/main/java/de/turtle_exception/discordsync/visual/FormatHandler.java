package de.turtle_exception.discordsync.visual;

import com.google.common.collect.Sets;
import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.SyncMessage;
import de.turtle_exception.fancyformat.Format;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FormatHandler {
    private final DiscordSync plugin;

    private final String formatMinMin;
    private final String formatDisMin;
    private final String formatMinDis;
    private final String formatDisDis;

    private record MinToken(@NotNull String token,
                            @NotNull Function3<SyncMessage, Player, BaseComponent, List<BaseComponent>> func
    ) {
        public @NotNull String get() {
            return "%" + token + "%";
        }
    }

    private record DisToken(@NotNull String token,
                            @NotNull Function2<SyncMessage, MessageChannel, String> func
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
        register(new MinToken("user"   , (message, player, component) -> replaceAll(component, "user"   , message.author().getName()))                         , true, true);
        register(new MinToken("message", (message, player, component) -> replaceAll(component, "message", message.content().toString(Format.MINECRAFT_LEGACY))), true, true);
        register(new MinToken("player" , (message, player, component) -> replaceAll(component, "player" , message.sourceInfo().getPlayer().getDisplayName()))  , true, false);
        register(new MinToken("dc_nick", (message, player, component) -> replaceAll(component, "dc_nick", message.sourceInfo().getEffectiveDiscordName()))     , false, true);
        register(new MinToken("dc_name", (message, player, component) -> replaceAll(component, "dc_name", message.sourceInfo().getUser().getName()))           , false, true);
        register(new MinToken("dc_tag" , (message, player, component) -> replaceAll(component, "dc_tag" , message.sourceInfo().getUser().getAsTag()))          , false, true);
        register(new MinToken("channel", (message, player, component) -> replaceAll(component, "channel", message.sourceInfo().getChannel().getName()))        , false, true);
        register(new MinToken("guild"  , (message, player, component) -> replaceAll(component, "guild"  , getGuild(message)))                                  , false, true);
        // TODO: mention?

        register(new DisToken("user"   , (message, channel) -> message.author().getName())                       , true , true );
        register(new DisToken("message", (message, channel) -> message.content().toString(Format.DISCORD))       , true , true );
        register(new DisToken("player" , (message, channel) -> message.sourceInfo().getPlayer().getDisplayName()), true , false);
        register(new DisToken("dc_nick", (message, channel) -> message.sourceInfo().getEffectiveDiscordName())   , false, true );
        register(new DisToken("dc_name", (message, channel) -> message.sourceInfo().getUser().getName())         , false, true );
        register(new DisToken("dc_tag" , (message, channel) -> message.sourceInfo().getUser().getAsTag())        , false, true );
        register(new DisToken("channel", (message, channel) -> message.sourceInfo().getChannel().getName())      , false, true );
        register(new DisToken("mention", (message, channel) -> message.sourceInfo().getUser().getAsMention())    , false, true );
        register(new DisToken("guild"  , (message, channel) -> getGuild(message))                                , false, true );
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
                if (!current.toPlainText().contains(token.get())) continue;

                // apply function -> split component into one or more new components
                List<BaseComponent> res = token.func().invoke(message, recipient, current);

                // replace component(s)
                components.remove(i);
                components.addAll(i, res);

                // iterator should continue after the added components (-1 because i will be incremented)
                i += res.size() - 1;
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
            format = format.replaceAll(token.get(), token.func().invoke(message, recipient));

        return format;
    }

    /* - - - */

    private @NotNull String getEmote(@NotNull SyncMessage message, long target) {
        return message.sourceInfo().isMinecraft()
                ? plugin.getEmoteHandler().getEmote(message.sourceInfo().getPlayer(), target)
                : plugin.getEmoteHandler().getEmote(message.sourceInfo().getChannel());
    }

    private @NotNull String getGuild(@NotNull SyncMessage message) {
        Member member = message.sourceInfo().getMember();
        return member != null
                ? member.getGuild().getName()
                : "PRIVATE";
    }

    /* - - - */

    private @NotNull List<BaseComponent> replaceAll(@NotNull BaseComponent component, @NotNull String token, @NotNull String replacement) {
        String legacyText = component.toLegacyText();
        legacyText = legacyText.replaceAll("%" + token + "%", replacement);
        return Arrays.asList(TextComponent.fromLegacyText(legacyText));
    }
}
