package de.turtle_exception.discordsync;

import de.turtle_exception.discordsync.channel.Channel;
import de.turtle_exception.discordsync.channel.ChannelCommand;
import de.turtle_exception.discordsync.channel.ChannelMapper;
import de.turtle_exception.discordsync.events.SyncChannelCreateEvent;
import de.turtle_exception.discordsync.events.SyncChannelDeleteEvent;
import de.turtle_exception.discordsync.events.SyncUserCreateEvent;
import de.turtle_exception.discordsync.events.SyncUserDeleteEvent;
import de.turtle_exception.discordsync.listeners.ChatListener;
import de.turtle_exception.discordsync.listeners.PresenceListener;
import de.turtle_exception.discordsync.listeners.UserListener;
import de.turtle_exception.discordsync.util.EntitySet;
import de.turtle_exception.discordsync.util.JDALogFilter;
import de.turtle_exception.discordsync.util.time.TurtleType;
import de.turtle_exception.discordsync.util.time.TurtleUtil;
import de.turtle_exception.discordsync.visual.AvatarHandler;
import de.turtle_exception.discordsync.visual.EmoteHandler;
import de.turtle_exception.discordsync.visual.FormatHandler;
import de.turtle_exception.fancyformat.FancyFormatter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DiscordSync extends JavaPlugin {
    private final EntitySet<SyncUser> userCache = new EntitySet<>();

    private ChannelMapper channelMapper;
    private final EntitySet<Channel> channelCache = new EntitySet<>();

    private FormatHandler formatHandler;
    private FancyFormatter formatter;

    private JDALogFilter jdaLogFilter;
    private JDA jda;

    private AvatarHandler avatarHandler;
    private EmoteHandler  emoteHandler;

    public DiscordSync() { }

    @Override
    public void onEnable() {
        this.userCache.clear();
        this.channelCache.clear();

        // CONFIG
        this.saveResource("users.yml", false);
        this.saveResource("channels.yml", false);
        this.saveResource("emotes.yml", false);
        this.saveDefaultConfig();
        this.reloadConfig();

        this.formatHandler = new FormatHandler(this);
        this.formatter = new FancyFormatter();

        // COMMANDS
        ChannelCommand channelCommandHandler = new ChannelCommand(this);
        PluginCommand channelCommand = this.getCommand("channel");
        if (channelCommand == null)
            throw new AssertionError("Channel command not implemented!");
        channelCommand.setTabCompleter(channelCommandHandler);
        channelCommand.setExecutor(channelCommandHandler);

        // LISTENERS
        this.getServer().getPluginManager().registerEvents(new PresenceListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        this.getServer().getPluginManager().registerEvents(new UserListener(this), this);

        // JDA
        try {
            jdaLogFilter = new JDALogFilter(this);
            jdaLogFilter.setLevel(Level.WARNING);
            jda = getJDABuilder().build();
            jda.awaitReady();
        } catch (InvalidTokenException e) {
            getLogger().log(Level.SEVERE, "Invalid token! Please make sure to set your Bot token in the config.yml");
            throw e;
        } catch (InterruptedException e) {
            getLogger().log(Level.SEVERE, "Encountered an exception when attempting to await JDA status READY", e);
            throw new RuntimeException(e);
        }

        // VISUALS
        this.avatarHandler = new AvatarHandler(this);
        this.emoteHandler  = new EmoteHandler(this);

        // USERS
        this.reloadUsers();

        // CHANNELS
        this.channelMapper = new ChannelMapper(this);
        this.reloadChannels();
    }

    @Override
    public void onDisable() {
        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        jda.getPresence().setActivity(null);

        jda.shutdown();

        this.jdaLogFilter.stop();
        this.jdaLogFilter = null;

        this.saveConfig();
        this.saveUsers();
        this.saveChannels();
    }

    public void reloadUsers() {
        this.userCache.clear();

        YamlConfiguration userYaml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "users.yml"));
        for (String key : userYaml.getKeys(false)) {
            long         id        = Long.parseLong(key);
            String       name      = userYaml.getString(key + ".name", "???");
            List<String> minecraft = userYaml.getStringList(key + ".minecraft");
            List<String> discord   = userYaml.getStringList(key + ".discord");

            userCache.put(new SyncUser(id, name,
                    minecraft.stream().map(UUID::fromString).toList(),
                    discord.stream().map(Long::parseLong).toList()
            ));
        }

        getLogger().log(Level.INFO, "Loaded " + userCache.size() + " user(s).");
    }

    public void saveUsers() {
        YamlConfiguration userYaml = new YamlConfiguration();

        for (SyncUser user : userCache) {
            long         id        = user.id();
            String       name      = user.getName();
            List<String> minecraft = user.getMinecraftIds().stream().map(UUID::toString).toList();
            List<String> discord   = user.getDiscordIds().stream().map(Object::toString).toList();

            userYaml.set(id + ".name", name);
            userYaml.set(id + ".minecraft", minecraft);
            userYaml.set(id + ".discord", discord);
        }

        try {
            userYaml.save(new File(getDataFolder(), "users.yml"));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not save user config!", e);
        }
    }

    public void reloadChannels() {
        this.channelCache.clear();

        YamlConfiguration channelYaml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "channels.yml"));
        for (String key : channelYaml.getKeys(false)) {
            long         id         = Long.parseLong(key);
            String       name       = channelYaml.getString(key + ".name", "unknown");
            List<String> worlds     = channelYaml.getStringList(key + ".worlds");
            List<Long>   snowflakes = channelYaml.getLongList(key + ".discord");

            channelCache.put(new Channel(id, this, name, worlds, snowflakes));
        }
    }

    public void saveChannels() {
        YamlConfiguration channelYaml = new YamlConfiguration();

        for (Channel channel : channelCache) {
            long       id         = channel.id();
            String     name       = channel.getName();
            List<UUID> worldUUIDs = channel.getWorlds();
            List<Long> snowflakes = channel.getSnowflakes();

            ArrayList<String> worlds = new ArrayList<>();
            if (worldUUIDs != null)
                worlds.addAll(worldUUIDs.stream().map(UUID::toString).toList());
            else
                worlds.add("*");

            channelYaml.set(id + ".name", name);
            channelYaml.set(id + ".worlds", worlds);
            channelYaml.set(id + ".discord", snowflakes);
        }

        try {
            channelYaml.save(new File(getDataFolder(), "channels.yml"));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not save user config!", e);
        }
    }

    /* - USERS - */

    public void createUser(@NotNull SyncUser user) {
        this.userCache.add(user);
        getServer().getPluginManager().callEvent(new SyncUserCreateEvent(user));
    }

    public void deleteUser(@NotNull SyncUser user) {
        this.userCache.remove(user);
        getServer().getPluginManager().callEvent(new SyncUserDeleteEvent(user));
    }

    public @Nullable SyncUser getUser(@NotNull UUID uuid) {
        for (SyncUser user : this.userCache)
            if (user.getMinecraftIds().contains(uuid))
                return user;
        return null;
    }

    public @Nullable SyncUser getUser(long snowflake) {
        for (SyncUser user : this.userCache)
            if (user.getDiscordIds().contains(snowflake))
                return user;
        return null;
    }

    public @NotNull SyncUser putUser(@NotNull UUID uuid, @NotNull String name) {
        if (getUser(uuid) != null)
            throw new IllegalStateException("May not overwrite user!");
        SyncUser user = new SyncUser(TurtleUtil.newId(TurtleType.USER), name, List.of(uuid), List.of());
        this.userCache.put(user);
        return user;
    }

    public @NotNull SyncUser putUser(long snowflake, @NotNull String name) {
        if (getUser(snowflake) != null)
            throw new IllegalStateException("May not overwrite user!");
        SyncUser user = new SyncUser(TurtleUtil.newId(TurtleType.USER), name, List.of(), List.of(snowflake));
        this.userCache.put(user);
        return user;
    }

    /* - CHANNELS - */

    public void createChannel(@NotNull Channel channel) {
        this.channelCache.add(channel);
        getServer().getPluginManager().callEvent(new SyncChannelCreateEvent(channel));
    }

    public void deleteChannel(@NotNull Channel channel) {
        this.channelCache.remove(channel);
        getServer().getPluginManager().callEvent(new SyncChannelDeleteEvent(channel));
    }

    public @NotNull ChannelMapper getChannelMapper() {
        return channelMapper;
    }

    public @NotNull EntitySet<Channel> getChannelCache() {
        return channelCache;
    }

    /* - - - */

    public @NotNull AvatarHandler getAvatarHandler() {
        return avatarHandler;
    }

    public @NotNull FormatHandler getFormatHandler() {
        return formatHandler;
    }

    public @NotNull EmoteHandler getEmoteHandler() {
        return emoteHandler;
    }

    public @NotNull FancyFormatter getFormatter() {
        return formatter;
    }

    public @NotNull JDA getJDA() {
        return jda;
    }

    private @NotNull JDABuilder getJDABuilder() {
        String token = getConfig().getString("discordToken");

        boolean emojis = getConfig().getBoolean("useEmojis", true);
        boolean onlineStatus = getConfig().getBoolean("onlineStatus", true);
        boolean directMessages = getConfig().getBoolean("directMessages", true);
        boolean typingIndicator = getConfig().getBoolean("typingIndicator", false);

        JDABuilder builder = JDABuilder.create(token,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
        ).disableCache(
                // we have to disable these manually because otherwise JDA will complain about missing intents
                CacheFlag.ACTIVITY,
                CacheFlag.VOICE_STATE,
                CacheFlag.EMOJI,
                CacheFlag.STICKER,
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ONLINE_STATUS,
                CacheFlag.SCHEDULED_EVENTS
        );

        // INTENTS
        if (emojis)
            builder.enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                    .enableCache(CacheFlag.EMOJI);
        if (onlineStatus)
            builder.enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableCache(
                            CacheFlag.ACTIVITY,
                            CacheFlag.CLIENT_STATUS,
                            CacheFlag.ONLINE_STATUS
                    );
        if (typingIndicator)
            builder.enableIntents(GatewayIntent.GUILD_MESSAGE_TYPING);
        if (directMessages) {
            builder.enableIntents(GatewayIntent.DIRECT_MESSAGES);
            if (typingIndicator)
                builder.enableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING);
        }

        // LISTENERS
        builder.addEventListeners(
                new ChatListener(this),
                new UserListener(this)
        );

        // PRESENCE
        builder.setActivity(Activity.of(Activity.ActivityType.PLAYING, "Minecraft"));
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);

        return builder;
    }
}
