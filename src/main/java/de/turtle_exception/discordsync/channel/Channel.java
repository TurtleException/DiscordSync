package de.turtle_exception.discordsync.channel;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.Entity;
import de.turtle_exception.discordsync.SyncMessage;
import de.turtle_exception.discordsync.util.EntityMap;
import de.turtle_exception.discordsync.util.FixedBlockingQueueMap;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class Channel implements Entity {
    private static Channel nullChannel;

    private final long id;
    private final DiscordSync plugin;
    private @NotNull String name;

    private final EntityMap<String, SyncMessage> messageCache;
    /** Discord response codes */
    private final FixedBlockingQueueMap<Long, Long> responseCodes;

    /** List of worlds. If this is {@code null} this Channel is server-wide. */
    private @Nullable List<UUID> worlds;
    /** List of Discord channels. */
    private @NotNull List<Long> snowflakes;

    public Channel(long id, @NotNull DiscordSync plugin, @NotNull String name, @NotNull List<String> worlds, @NotNull List<Long> snowflakes) {
        this.id = id;
        this.plugin = plugin;
        this.name = name;

        int backlog = plugin.getConfig().getInt("messageBacklog", 1000);
        messageCache = new EntityMap<>(new String[backlog], new SyncMessage[backlog]);
        responseCodes = new FixedBlockingQueueMap<>(new Long[backlog], new Long[backlog]);

        this.worlds    = new ArrayList<>();
        for (String world : worlds) {
            if (world.equals("*")) {
                this.worlds = null;
                break;
            }
            this.worlds.add(UUID.fromString(world));
        }
        this.registerListeners();

        this.snowflakes = snowflakes;

        for (Long snowflake : snowflakes) {
            GuildChannel channel = plugin.getJDA().getChannelById(GuildChannel.class, snowflake);
            if (channel == null) continue;
            plugin.getEmoteHandler().register(channel.getGuild());
        }
    }

    private void registerListeners() {
        if (worlds == null) {
            plugin.getChannelMapper().register(this);
        } else {
            for (UUID world : worlds)
                plugin.getChannelMapper().register(world, this);
        }
    }

    public static @NotNull Channel getNullChannel(@NotNull DiscordSync plugin) {
        if (nullChannel == null)
            nullChannel = new Channel(0, plugin, "null", List.of(), List.of());
        return nullChannel;
    }

    /* - - - */

    @Override
    public long id() {
        return this.id;
    }

    public @NotNull DiscordSync getPlugin() {
        return this.plugin;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public boolean isServer() {
        return worlds == null;
    }

    public @Nullable List<UUID> getWorlds() {
        return worlds;
    }

    public @NotNull List<Long> getSnowflakes() {
        return snowflakes;
    }

    /* - - - */

    public void send(@NotNull SyncMessage message) {
        // TODO: quick response code as key
        messageCache.put("", message);
        responseCodes.offer(message.id(), null);


        /* - MINECRAFT */
        String minecraftMsg = plugin.getFormatHandler().toMinecraft(message);
        BaseComponent[] component = TextComponent.fromLegacyText(minecraftMsg);
        getPlugin().getServer().getOnlinePlayers().stream()
                .filter(player -> getPlugin().getChannelMapper().get(player.getUniqueId()).equals(this))
                .forEach(player -> player.spigot().sendMessage(component));


        /* - DISCORD */
        for (Long snowflake : this.snowflakes) {
            // ignore if the message came from this channel
            if (message.sourceInfo().isFromChannel(snowflake)) return;

            MessageChannel channel = getPlugin().getJDA().getChannelById(MessageChannel.class, snowflake);
            if (channel == null) {
                getPlugin().getLogger().log(Level.WARNING, "Missing channel " + snowflake);
                return;
            }

            String discordMsg = plugin.getFormatHandler().toDiscord(message, snowflake);

            MessageCreateAction action = channel.sendMessage(
                    new MessageCreateBuilder()
                            .setContent(discordMsg)
                            .build());

            Long reference = responseCodes.get(message.reference());
            if (reference != null)
                action.setMessageReference(reference);

            // TODO: this would not work with multiple Discord endpoints (response code will be overwritten)

            action.queue(success -> {
                responseCodes.put(message.id(), success.getIdLong());
            }, throwable -> {
                getPlugin().getLogger().log(Level.WARNING, "Encountered an unexpected exception while attempting to send message " + message.id(), throwable);
                responseCodes.put(message.id(), null);
            });
        }
    }
}
