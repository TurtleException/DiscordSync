package de.turtle_exception.discordsync.visual;

import com.google.common.collect.Sets;
import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.SyncMessage;
import de.turtle_exception.fancyformat.Format;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiFunction;

public class FormatHandler {
    private final DiscordSync plugin;

    private final String formatMinMin;
    private final String formatDisMin;
    private final String formatMinDis;
    private final String formatDisDis;

    private record Token(@NotNull String token, @NotNull BiFunction<SyncMessage, Long, String> func) {
        public @NotNull String get() {
            return "%" + token + "%";
        }
    }

    private final Set<Token> tokensMinMin = Sets.newConcurrentHashSet();
    private final Set<Token> tokensDisMin = Sets.newConcurrentHashSet();
    private final Set<Token> tokensMinDis = Sets.newConcurrentHashSet();
    private final Set<Token> tokensDisDis = Sets.newConcurrentHashSet();

    @SuppressWarnings("Convert2MethodRef") // looks nicer
    public FormatHandler(DiscordSync plugin) {
        this.plugin = plugin;

        this.formatMinMin = plugin.getConfig().getString("format.minecraftChat.minecraft");
        this.formatDisMin = plugin.getConfig().getString("format.minecraftChat.discord");
        this.formatMinDis = plugin.getConfig().getString("format.discordChat.minecraft");
        this.formatDisDis = plugin.getConfig().getString("format.discordChat.discord");

        if (formatMinMin == null || formatDisMin == null || formatMinDis == null || formatDisDis == null)
            throw new NullPointerException("config.yml is missing one or more format settings!");

        register(new Token("user"   , (message, target) -> message.author().getName())                         , tokensMinMin, tokensDisMin, tokensMinDis, tokensDisDis);
        register(new Token("message", (message, target) -> message.content().toString(Format.MINECRAFT_LEGACY)), tokensMinMin, tokensDisMin                            );
        register(new Token("message", (message, target) -> message.content().toString(Format.DISCORD))         ,                             tokensMinDis, tokensDisDis);
        register(new Token("player" , (message, target) -> message.sourceInfo().getPlayer().getDisplayName())  , tokensMinMin,               tokensMinDis              );
        register(new Token("dc_nick", (message, target) -> message.sourceInfo().getEffectiveDiscordName())     ,               tokensDisMin,               tokensDisDis);
        register(new Token("dc_name", (message, target) -> message.sourceInfo().getUser().getName())           ,               tokensDisMin,               tokensDisDis);
        register(new Token("dc_tag" , (message, target) -> message.sourceInfo().getUser().getAsTag())          ,               tokensDisMin,               tokensDisDis);
        register(new Token("channel", (message, target) -> message.sourceInfo().getChannel().getName())        ,               tokensDisMin,               tokensDisDis);
        register(new Token("mention", (message, target) -> message.sourceInfo().getUser().getAsMention())      ,                                           tokensDisDis);
        register(new Token("emote"  , (message, target) -> getEmote(message, target))                          ,                             tokensMinDis, tokensDisDis);
        register(new Token("guild"  , (message, target) -> getGuild(message))                                  ,               tokensDisMin,               tokensDisDis);
    }

    @SafeVarargs
    private static void register(@NotNull Token token, Set<Token>... sets) {
        for (Set<Token> set : sets)
            set.add(token);
    }

    /* - - - */

    public @NotNull String toMinecraft(@NotNull SyncMessage message) {
        if (message.sourceInfo().isMinecraft()) {
            String format = this.formatMinMin;

            for (Token token : this.tokensMinMin)
                format = format.replaceAll(token.get(), token.func.apply(message, null));

            return format;
        } else {
            String format = this.formatDisMin;

            for (Token token : this.tokensDisMin)
                format = format.replaceAll(token.get(), token.func.apply(message, null));

            return format;
        }
    }

    public @NotNull String toDiscord(@NotNull SyncMessage message, long target) {
        if (message.sourceInfo().isMinecraft()) {
            String format = this.formatMinDis;

            for (Token token : this.tokensMinDis)
                format = format.replaceAll(token.get(), token.func.apply(message, target));

            return format;
        } else {
            String format = this.formatDisDis;

            for (Token token : this.tokensDisDis)
                format = format.replaceAll(token.get(), token.func.apply(message, target));

            return format;
        }
    }

    private @NotNull String getEmote(@NotNull SyncMessage message, long target) {
        if (message.sourceInfo().isMinecraft())
            return plugin.getEmoteHandler().getEmote(message.sourceInfo().getPlayer(), target);
        else
            return plugin.getEmoteHandler().getEmote(message.sourceInfo().getChannel());
    }

    private @NotNull String getGuild(@NotNull SyncMessage message) {
        Member member = message.sourceInfo().getMember();
        if (member != null)
            return member.getGuild().getName();
        else
            return "PRIVATE";
    }
}
