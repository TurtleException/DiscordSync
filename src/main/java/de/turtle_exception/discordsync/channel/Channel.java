package de.turtle_exception.discordsync.channel;

import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.Entity;
import de.turtle_exception.discordsync.SyncMessage;
import de.turtle_exception.discordsync.channel.endpoints.DiscordChannel;
import de.turtle_exception.discordsync.channel.endpoints.MinecraftServer;
import de.turtle_exception.discordsync.channel.endpoints.MinecraftWorld;
import de.turtle_exception.discordsync.util.EntityMap;
import de.turtle_exception.discordsync.util.EntitySet;
import de.turtle_exception.discordsync.util.FixedBlockingQueueMap;
import de.turtle_exception.fancyformat.Format;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class Channel implements Entity {
    private static Channel nullChannel;

    private final long id;
    private final DiscordSync plugin;
    private @NotNull String name;

    private final EntityMap<String, SyncMessage> messageCache;
    /** Discord response codes */
    // TODO: should this be locked?
    private final ConcurrentHashMap<Long, FixedBlockingQueueMap<Long, Long>> responseCodes;

    private final EntitySet<Endpoint> endpoints = new EntitySet<>();

    public Channel(long id, @NotNull DiscordSync plugin, @NotNull String name, @NotNull List<String> worlds, @NotNull List<Long> snowflakes) {
        this.id = id;
        this.plugin = plugin;
        this.name = name;

        int backlog = plugin.getConfig().getInt("messageBacklog", 1000);
        messageCache = new EntityMap<>(new String[backlog], new SyncMessage[backlog]);
        responseCodes = new ConcurrentHashMap<>(snowflakes.size());

        // populate responseCode map
        for (Long snowflake : snowflakes)
            responseCodes.put(snowflake, new FixedBlockingQueueMap<>(new Long[backlog], new Long[backlog]));

        // MINECRAFT ENDPOINTS
        if (worlds.contains("*"))
            this.endpoints.add(new MinecraftServer(this));
        else
            for (String world : worlds)
                this.endpoints.add(new MinecraftWorld(this, UUID.fromString(world)));

        // DISCORD ENDPOINTS
        for (Long snowflake : snowflakes) {
            this.endpoints.add(new DiscordChannel(this, snowflake));

            // register emotes
            GuildChannel channel = plugin.getJDA().getGuildChannelById(snowflake);
            if (channel == null) continue;
            plugin.getEmoteHandler().register(channel.getGuild());
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

    public @NotNull ConcurrentHashMap<Long, FixedBlockingQueueMap<Long, Long>> getResponseCodes() {
        return responseCodes;
    }

    public @Nullable List<UUID> getWorlds() {
        ArrayList<UUID> worlds = new ArrayList<>();
        for (Endpoint endpoint : endpoints) {
            if (endpoint instanceof MinecraftServer) return null;

            if (endpoint instanceof MinecraftWorld mWorld)
                worlds.add(mWorld.getUUID());
        }
        return List.copyOf(worlds);
    }

    public @NotNull List<Long> getSnowflakes() {
        return endpoints.stream()
                .filter(endpoint -> endpoint instanceof DiscordChannel)
                .map(endpoint -> ((DiscordChannel) endpoint).getSnowflake())
                .toList();
    }

    /* - - - */

    public void send(@NotNull SyncMessage message) {
        // TODO: quick response code as key
        messageCache.put("", message);
        // reserve space in message caches
        responseCodes.forEach((channel, cache) -> cache.offer(message.id(), null));

        // log chat message
        plugin.getServer().getLogger().log(Level.INFO, "<" + name + "> " + message.author().getName() + ":  " + message.content().toString(Format.PLAINTEXT));

        // pass message to endpoints
        for (Endpoint endpoint : this.endpoints)
            endpoint.send(message);
    }
}
