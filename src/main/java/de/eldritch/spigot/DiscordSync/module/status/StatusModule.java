package de.eldritch.spigot.DiscordSync.module.status;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.PluginModule;
import de.eldritch.spigot.DiscordSync.module.PluginModuleEnableException;
import de.eldritch.spigot.DiscordSync.util.DiscordUtil;
import de.eldritch.spigot.DiscordSync.util.Performance;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;

// TODO: update documentation
public class StatusModule extends PluginModule {
    private int taskId;

    private int errorIncrement;
    private int errorTolerance;

    private static final String EMBED_DESCRIPTION =
            "Der Bot versucht diese Nachricht laufend zu aktualisieren. "
          + "Da je nach Auslastung der *Discord API* oder des Servers einige dieser "
          + "Updates ausfallen könnten ist spätestens nach einer Minute damit zu "
          + "rechnen, dass der Server offline ist oder das Plugin nicht funktioniert.\n ";

    @Override
    public void onEnable() throws PluginModuleEnableException {
        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new PluginModuleEnableException("Module is dependant on JDA connection.");

        this.errorIncrement = 0;
        try {
            this.errorTolerance = getConfig().isInt("errorTolerance")
                    ? getConfig().getInt("errorTolerance")
                    : Integer.parseInt(getConfig().getString("errorTolerance", "null"));
        } catch (NumberFormatException e) {
            getLogger().log(Level.WARNING, "Property 'errorTolerance' (" + getConfig().getString("errorTolerance", "null") + ") should be of type int", e);
            this.errorTolerance = 10;
        }

        this.scheduleTask();
    }

    @Override
    public void onDisable() {
        this.cancelTask();

        this.updateMessage(this.buildMessage().clearFields()
                .addField(":red_circle: Offline", "Der Server wurde " + TimeFormat.RELATIVE.now() + " gestoppt.", false)
                .addField("Plugin Version", "`" + DiscordSync.singleton.getVersion().toString() + "`", false)
                .build()
        );
    }

    /**
     * Schedules the task to repeatedly call {@link StatusModule#updateMessage(MessageEmbed)}.
     * To prevent multiple tasks from running simultaneously {@link StatusModule#cancelTask()}
     * is called first.
     * @see StatusModule#onEnable()
     */
    private void scheduleTask() {
        this.cancelTask();

        long interval;
        try {
            interval = getConfig().isLong("interval") ? getConfig().getLong("interval")
                    : Long.parseLong(getConfig().getString("interval", "null"));
        } catch (NumberFormatException e) {
            getLogger().log(Level.WARNING, "Property 'interval' (" + getConfig().getString("interval", "null") + ") should be of type int", e);
            interval = 100L;
        }

        this.taskId = DiscordSync.singleton.getServer().getScheduler().scheduleSyncRepeatingTask(
                DiscordSync.singleton, () -> this.updateMessage(this.buildMessage().build()), 60L, interval
        );
    }

    /**
     * Checks whether the module currently has a task running and cancels that task.
     * @see StatusModule#scheduleTask()
     * @see StatusModule#onDisable()
     */
    public void cancelTask() {
        if (DiscordSync.singleton.getServer().getScheduler().isCurrentlyRunning(taskId))
            DiscordSync.singleton.getServer().getScheduler().cancelTask(taskId);
    }

    /**
     * Fetches the status information and builds a {@link MessageEmbed}.
     * @return {@link MessageEmbed} filled with current information.
     */
    public EmbedBuilder buildMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Server Status")
                .setDescription("Letztes Update: " + TimeFormat.RELATIVE.now() + ".\n\n> " + EMBED_DESCRIPTION)
                .setColor(DiscordUtil.COLOR_NEUTRAL)
                .setFooter(DiscordUtil.FOOTER_TEXT, DiscordUtil.getAvatarURL())
                .setTimestamp(DiscordUtil.getTimestamp());

        // set thumbnail
        try {
            String url = "http://cdn.eldritch.de/mc/EldritchDiscord/" + DiscordSync.singleton.getServerName() + ".png";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            if (connection.getResponseCode() != 200) throw new IOException();
            builder.setThumbnail(url);
        } catch (IOException ignored) {
            builder.setThumbnail("http://cdn.eldritch.de/mc/EldritchDiscord/unknown.png");
        }

        // online players (per world)
        Map<String, Collection<Player>> players = this.getOnlinePlayers();
        if (players.isEmpty()) {
            builder.addField("Spieler", "Keine Spieler online :c", true);
        } else {
            players.forEach((world, players1) -> {
                StringBuilder str = new StringBuilder();
                players1.forEach(player -> {
                    try {
                        // TODO: cleanup
                        str.append(Objects.requireNonNull(Objects.requireNonNull(DiscordSync.singleton.getDiscordAPI()).getGuild()).getEmotesByName(player.getName(), false).get(0).getAsMention());
                    } catch (NullPointerException | IndexOutOfBoundsException ignored) {
                        str.append(":question:");
                    }
                    str.append(" ").append(player.getDisplayName()).append("\n");
                });

                // custom emote
                String emote = "";
                if (Objects.requireNonNull(getConfig().getConfigurationSection("emotes")).contains(world)) {
                    // TODO: cleanup
                    emote = Objects.requireNonNull(getConfig().getConfigurationSection("emotes")).getString(world) + " ";
                }

                // check for a custom world name in the config
                try {
                    if (Objects.requireNonNull(getConfig().getConfigurationSection("worlds")).contains(world)) {
                        // TODO: cleanup
                        builder.addField(emote + "Spieler | " + Objects.requireNonNull(getConfig().getConfigurationSection("worlds")).get(world), str.toString(), true);
                    }
                } catch (NullPointerException ignored) {
                    builder.addField(emote + "Spieler | " + world, str.toString(), true);
                }
            });
        }

        builder.addField("Performance", "`" + new DecimalFormat("00.00").format(Performance.getTPS(100)) + "` TPS  (" + Performance.getLagPercent() + "% Lag)", false);

        builder.addField("Plugin Version", "`" + DiscordSync.singleton.getVersion().toString() + "`", false);

        return builder;
    }

    /**
     * Updates the Discord status embed message.
     * <p>
     *     Each time the message fails to update properly an integer is incremented
     *     until it reaches a tolerance (set via config). At this point a new
     *     message is created which will be the message that is updated repeatedly.
     * </p>
     * @param embed The new embed version.
     */
    private void updateMessage(MessageEmbed embed) {
        if (DiscordSync.singleton.getDiscordAPI() == null || DiscordSync.singleton.getDiscordAPI().getGuild() == null)
            return;

        long channelId, messageId;
        try {
            channelId = getConfig().isLong("discord.channel")
                    ? getConfig().getLong("discord.channel")
                    : Long.parseLong(getConfig().getString("discord.channel", "null"));
        } catch (NumberFormatException e) {
            getLogger().log(Level.WARNING, "Property 'discord.channel' (" + getConfig().getString("discord.channel", "null")
                    + ") should be of type long", e);
            for (MessageEmbed.Field field : embed.getFields()) {
                if (field.getName() != null && (field.getName().contains("Offline") || field.getName().contains("offline"))) {
                    // prevent infinite loop
                    return;
                }
            }
            DiscordSync.singleton.getModuleManager().unregister(this);
            return;
        }

        try {
            messageId = getConfig().isLong("discord.message")
                    ? getConfig().getLong("discord.message")
                    : Long.parseLong(getConfig().getString("discord.message", "null"));
        } catch (NumberFormatException e) {
            getLogger().log(Level.WARNING, "Property 'discord.message' (" + getConfig().getString("discord.message", "null")
                    + ") should be of type long", e);
            messageId = 0L;
            errorIncrement = errorTolerance;
        }

        TextChannel channel = DiscordSync.singleton.getDiscordAPI().getGuild().getTextChannelById(channelId);
        if (channel == null) return;

        channel.retrieveMessageById(messageId).queue((message) -> {
            message.editMessageEmbeds(embed).queue();
            errorIncrement = 0;
        }, (failure) -> {
            errorIncrement++;
            getLogger().log(Level.WARNING, "Unable to update status message.", failure);
            if (errorIncrement > errorTolerance && errorTolerance >= 0) {
                getLogger().log(Level.WARNING, "Creating new message...");
                errorIncrement = 0;
                // see https://stackoverflow.com/a/59805069 for further information on how to proceed here
                channel.sendMessageEmbeds(embed).queue(newMessage -> {
                    // save new message id
                    getConfig().set("discord.message", newMessage.getIdLong());
                });
            }
        });
    }

    /**
     * Provides a {@link Collection} of all online {@link Player Players}
     * per world as a {@link Map}. Each key is the name of a world, the
     * values are {@link Collection Collections} of players in that world.
     */
    public Map<String, Collection<Player>> getOnlinePlayers() {
        Collection<? extends Player> players = DiscordSync.singleton.getServer().getOnlinePlayers();
        Map<String, Collection<Player>> playerMap = new HashMap<>();

        // organize players by world name
        for (Player player : players) {
            if (playerMap.containsKey(player.getWorld().getName())) {
                playerMap.get(player.getWorld().getName()).add(player);
            } else {
                HashSet<Player> worldPlayers = new HashSet<>();
                worldPlayers.add(player);
                playerMap.put(player.getWorld().getName(), worldPlayers);
            }
        }

        return playerMap;
    }
}
