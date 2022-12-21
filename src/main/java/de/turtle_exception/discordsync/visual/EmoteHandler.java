package de.turtle_exception.discordsync.visual;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.turtle_exception.discordsync.DiscordSync;
import de.turtle_exception.discordsync.channel.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;

public class EmoteHandler implements Listener {
    private final DiscordSync plugin;

    private final Object tableLock = new Object();

    // Author, Channel (target), Emoji
    private final Table<UUID, Long, Emoji> playerEmoji = HashBasedTable.create();
    private final Map<Long, Emoji> guildEmoji = new ConcurrentHashMap<>();

    private final Emoji playerDefault = Emoji.fromUnicode("\u2753");
    private final Emoji  guildDefault = Emoji.fromUnicode("\u2753");

    private final boolean enabled;

    private final boolean playerCircular;
    private final boolean  guildCircular;

    public EmoteHandler(@NotNull DiscordSync plugin) {
        this.plugin = plugin;

        this.enabled = plugin.getConfig().getBoolean("useEmoji");

        this.playerCircular = plugin.getConfig().getBoolean("emote.playerCircular", false);
        this.guildCircular  = plugin.getConfig().getBoolean("emote.guildCircular", true);

        if (enabled)
            this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

        // load from config
        YamlConfiguration emoteYaml = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "emotes.yml"));
        ConfigurationSection players = emoteYaml.getConfigurationSection("players");
        ConfigurationSection guilds  = emoteYaml.getConfigurationSection("guilds");

        if (players != null) {
            for (String key : players.getKeys(false)) {
                UUID uuid = UUID.fromString(key);

                ConfigurationSection guildSection = players.getConfigurationSection(key);
                if (guildSection == null) continue;

                for (String guildId : guildSection.getKeys(false)) {
                    Guild guild = plugin.getJDA().getGuildById(guildId);
                    long  emote = guildSection.getLong(guildId);
                    if (guild == null) continue;
                    Emoji emoji = guild.getEmojiById(emote);
                    if (emoji == null) continue;

                    synchronized (tableLock) {
                        playerEmoji.put(uuid, guild.getIdLong(), emoji);
                    }
                }
            }
        }

        if (guilds != null) {
            for (String key : guilds.getKeys(false)) {
                Guild guild = plugin.getJDA().getGuildById(key);
                long  emote = guilds.getLong(key);
                if (guild == null) continue;
                Emoji emoji = guild.getEmojiById(emote);
                if (emoji == null) continue;

                synchronized (tableLock) {
                    guildEmoji.put(guild.getIdLong(), emoji);
                }
            }
        }
    }

    /* - - - */

    public @NotNull String getEmote(@NotNull OfflinePlayer player, long target) {
        Emoji emoji;
        synchronized (tableLock)  {
            emoji = playerEmoji.get(player.getUniqueId(), target);
        }

        if (emoji == null)
            emoji = playerDefault;
        return emoji.getFormatted();
    }

    public @NotNull String getEmote(@NotNull MessageChannelUnion channel) {
        Emoji emoji = null;
        if (channel instanceof GuildChannel guildChannel) {
            synchronized (tableLock) {
                emoji = guildEmoji.get(guildChannel.getGuild().getIdLong());
            }
        }

        if (emoji == null)
            emoji = guildDefault;
        return emoji.getFormatted();
    }

    /* - - - */

    public void register(@NotNull OfflinePlayer player) {
        if (!enabled) return;

        if (player.getName() == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot create emote for player " + player.getUniqueId() + " as they have no name!");
            return;
        }

        HashSet<Guild> guilds = new HashSet<>();

        for (Channel channel : plugin.getChannelCache()) {
            for (Long snowflake : channel.getSnowflakes()) {
                MessageChannel messageChannel = plugin.getJDA().getChannelById(MessageChannel.class, snowflake);

                if (messageChannel instanceof GuildMessageChannel guildChannel)
                    guilds.add(guildChannel.getGuild());
            }
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Icon icon = getImage(plugin.getAvatarHandler().getHead(player), playerCircular);

                // add emote for all guilds
                for (Guild guild : guilds) {
                    // delete old emoji
                    synchronized (tableLock) {
                        Emoji emoji = playerEmoji.get(player.getUniqueId(), guild.getIdLong());

                        if (emoji instanceof RichCustomEmoji rce)
                            rce.delete().complete();
                    }

                    // create emoji
                    RichCustomEmoji emoji = guild.createEmoji(player.getName(), icon).complete();

                    // cache emoji
                    synchronized (tableLock) {
                        playerEmoji.put(player.getUniqueId(), guild.getIdLong(), emoji);
                    }

                    // write cache to file
                    this.save("player." + player.getUniqueId() + "." + guild.getId(), emoji.getIdLong());
                }

                plugin.getLogger().log(Level.INFO, "Created emoji \"" + player.getName() + "\" for " + guilds.size() + " guilds.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not retrieve image for player " + player.getUniqueId(), e);
            } catch (RejectedExecutionException ignored) {
                // JDA is shutting down
            } catch (InsufficientPermissionException e) {
                plugin.getLogger().log(Level.WARNING, "Insufficient permissions to create guild emoji for player " + player.getName());
                plugin.getLogger().log(Level.WARNING, "Please make sure the bot has the MANAGE_EMOJIS_AND_STICKERS permission!");
                plugin.getLogger().log(Level.WARNING, "This error message will keep appearing until the issue is resolved.", e);
            }
        });
    }

    public void register(@NotNull Guild guild) {
        if (!enabled) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // delete old emoji
                synchronized (tableLock) {
                    Emoji emoji = guildEmoji.get(guild.getIdLong());

                    if (emoji instanceof RichCustomEmoji rce)
                        rce.delete().complete();
                }

                String url  = guild.getIconUrl();
                String name = guild.getName().replaceAll(" ", "");

                if (url == null) return;

                Icon icon = getImage(url, guildCircular);

                // only add the emote for the guild itself
                RichCustomEmoji emoji = guild.createEmoji(name, icon).complete();

                // cache emoji
                synchronized (tableLock) {
                    guildEmoji.put(guild.getIdLong(), emoji);
                }

                // write cache to file
                this.save("guild." + guild.getId(), emoji.getIdLong());

                plugin.getLogger().log(Level.INFO, "Created emoji \"" + name + "\".");
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not retrieve image for guild " + guild.getId(), e);
            } catch (RejectedExecutionException ignored) {
                // JDA is shutting down
            } catch (InsufficientPermissionException e) {
                plugin.getLogger().log(Level.WARNING, "Insufficient permissions to create guild emoji for guild " + guild.getName());
                plugin.getLogger().log(Level.WARNING, "Please make sure the bot has the MANAGE_EMOJIS_AND_STICKERS permission!");
                plugin.getLogger().log(Level.WARNING, "This error message will keep appearing until the issue is resolved.", e);
            }
        });
    }

    /* - - - */

    private @NotNull Icon getImage(@NotNull String url, boolean circular) throws IOException {
        BufferedImage image = ImageIO.read(new URL(url));
        int size = image.getWidth();

        if (circular) {
            Graphics2D graphics = image.createGraphics();

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.fillOval(0, 0, size, size);

            graphics.setComposite(AlphaComposite.SrcIn);
            graphics.drawImage(image, 0, 0, null);

            graphics.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());

        // ByteArrayStreams don't need to be closed

        return Icon.from(in, Icon.IconType.PNG);
    }

    /* - - - */

    private void save(@NotNull String path, long id) {
        try {
            File file = new File(plugin.getDataFolder(), "emotes.yml");
            YamlConfiguration emoteYaml = YamlConfiguration.loadConfiguration(file);
            emoteYaml.set(path, id);
            emoteYaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.FINE, "Encountered an IOException when attempting to save emote id (" + path + ":" + id + ")", e);
        }
    }

    /* - - - */

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        this.register(event.getPlayer());
    }
}
