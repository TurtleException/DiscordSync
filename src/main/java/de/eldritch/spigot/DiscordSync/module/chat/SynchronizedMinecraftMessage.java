package de.eldritch.spigot.DiscordSync.module.chat;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SynchronizedMinecraftMessage {
    private final String message;
    private final Player author;

    private String replyTarget = null;

    public SynchronizedMinecraftMessage(String message, Player author) {
        this.author = author;
        String processedMessage = message;


        String[] tokens = message.split(" ");

        // remove response-prefix from actual message
        if (message.startsWith("@") && tokens.length > 0) {
            processedMessage = message.substring(tokens[0].length() + " ".length());
            replyTarget = tokens[0].substring(1);
        }

        this.message = processedMessage;
    }

    /**
     * Formats the message to be compatible with
     * the Discord chat.
     */
    public String toDiscord() {
        String emote = "";
        if (DiscordSync.singleton.getDiscordAPI() != null) {
            try {
                emote = Objects.requireNonNull(Objects.requireNonNull(DiscordSync.singleton.getDiscordAPI()).getGuild()).getEmotesByName(author.getName(), false).get(0).getAsMention() + " ";
            } catch (NullPointerException | IndexOutOfBoundsException ignored1) {
                try {
                    emote = Objects.requireNonNull(DiscordSync.singleton.getDiscordAPI().getGuild()).getEmotesByName("minecraft", true).get(0).getAsMention() + " ";
                } catch (NullPointerException | IndexOutOfBoundsException ignored2) {
                    emote = "";
                }
            }
        }

        String str = emote + "__**" + author.getDisplayName() + "**:__  " + message;

        if (str.length() >= 2000)
            str = str.substring(0, 1996) + "...";

        return str;
    }

    public void send() {
        DiscordSync.singleton.getServer().spigot().broadcast(
                MessageService.get(
                        "module.chat.message.bare.minecraft.name",
                        author.getDisplayName()
                ),
                MessageService.get(
                        "module.chat.message.bare.message",
                        formatNewline(message)
                )
        );
    }

    public void send(@NotNull Member replyTargetAuthor, @NotNull Message replyTarget) {
        DiscordSync.singleton.getServer().spigot().broadcast(
                MessageService.get(
                        "module.chat.message.bare.minecraft.name",
                        author.getDisplayName()
                ),
                MessageService.get(
                        "module.chat.message.bare.reply",
                        replyTargetAuthor.getEffectiveName(),
                        replyTarget.getId(),
                        replyTarget.getContentStripped()
                ),
                MessageService.get(
                        "module.chat.message.bare.message",
                        formatNewline(message)
                )
        );
    }

    /**
     * @return Message id of the reply target, <code>null</code>
     *         if the message is not a reply.
     */
    public @Nullable String getReplyTarget() {
        return this.replyTarget;
    }

    private String formatNewline(String message) {
        return WordUtils.wrap(message, 75, "\n", true);
    }
}
