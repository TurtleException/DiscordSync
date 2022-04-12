package de.eldritch.spigot.discord_sync.discord;

import de.eldritch.spigot.discord_sync.DiscordSync;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;

public class Accessor {
    /**
     * The guild that is synchronized with the Minecraft server.
     */
    private final Guild guild;

    /**
     * The main message channel that is synchronized with the Minecraft chat.
     */
    TextChannel messageChannel;
    /**
     * Contains updates on who is joining / leaving the server.
     */
    TextChannel joinLeaveChannel;
    /**
     * Contains {@link Advancement} notifications.
     */
    TextChannel advancementChannel;
    /**
     * Contains player death notifications.
     */
    TextChannel deathChannel;

    Accessor(DiscordService service) throws NumberFormatException, NullPointerException {
        guild = service.getJDA().getGuildById(getID("guild"));

        if (guild == null)
            throw new NullPointerException("Guild may not be null");

        messageChannel     = loadChannel("message");
        joinLeaveChannel   = loadChannel("joinLeave");
        advancementChannel = loadChannel("advancement");
        deathChannel       = loadChannel("death");
    }

    public enum Channel { MESSAGE, JOIN_LEAVE, ADVANCEMENT, DEATH }

    public MessageAction send(Channel channel, Message msg) {
        return switch (channel) {
            case MESSAGE     -> messageChannel.sendMessage(msg);
            case JOIN_LEAVE  -> joinLeaveChannel.sendMessage(msg);
            case ADVANCEMENT -> advancementChannel.sendMessage(msg);
            case DEATH       -> deathChannel.sendMessage(msg);
        };
    }

    /* ----- ----- ----- */

    private @NotNull TextChannel loadChannel(String idPath) throws NumberFormatException, NullPointerException {
        TextChannel channel = guild.getTextChannelById(getID("channel." + idPath));

        if (channel == null)
            throw new NullPointerException("Channel \"" + idPath + "\" may not be null.");

        return channel;
    }

    private static String getID(String entity) {
        return DiscordSync.singleton.getConfig().getString("discord." + entity, "null");
    }

    /* ----- ----- ----- */

    public Guild getGuild() {
        return guild;
    }
}
