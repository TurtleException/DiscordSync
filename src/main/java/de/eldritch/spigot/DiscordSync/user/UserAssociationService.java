package de.eldritch.spigot.DiscordSync.user;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.user.command.CommandVerify;
import de.eldritch.spigot.DiscordSync.user.listener.DiscordNameListener;
import de.eldritch.spigot.DiscordSync.user.listener.DiscordVerificationListener;
import de.eldritch.spigot.DiscordSync.user.listener.MinecraftRegisterListener;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public class UserAssociationService {
    private final HashSet<User> users = new HashSet<>();

    private final YamlConfiguration config = new YamlConfiguration();
    private final File configFile;

    public UserAssociationService() throws NullPointerException, IOException {
        if (DiscordSync.singleton.getDiscordAPI() == null)
            throw new NullPointerException("Module is dependant on JDA connection.");


        File[] files = DiscordSync.singleton.getDataFolder().listFiles((dir, name) -> name.equals("users.yml"));
        if (files == null || files.length == 0) {
            DiscordSync.singleton.getLogger().info("users.yml does not exist. Attempting to create a new file...");
            try {
                if (!DiscordSync.singleton.getDataFolder().exists() && !DiscordSync.singleton.getDataFolder().mkdir())
                    throw new IOException("Could not create plugin data folder.");
                new File(DiscordSync.singleton.getDataFolder(), "users.yml").createNewFile();

                DiscordSync.singleton.getLogger().info("users.yml created!");
            } catch (IOException e) {
                throw new IOException("Unable to access users.yml.", e);
            }
        }

        try {
            configFile = Objects.requireNonNull(files)[0];
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new IOException("Unable to load users.yml", e);
        }


        /* register listeners */
        DiscordSync.singleton.getDiscordAPI().getJDA().addEventListener(new DiscordNameListener());
        DiscordSync.singleton.getDiscordAPI().getJDA().addEventListener(new DiscordVerificationListener());
        DiscordSync.singleton.getServer().getPluginManager().registerEvents(new MinecraftRegisterListener(), DiscordSync.singleton);

        /* register commands */
        Objects.requireNonNull(DiscordSync.singleton.getCommand("verify")).setExecutor(new CommandVerify());
    }

    public @Nullable User get(@NotNull Predicate<User> predicate) {
        return users.stream().filter(predicate).findAny().orElse(null);
    }

    public void registerUser(@NotNull User user) {
        users.add(user);
    }

    public void update(Member member, String name) {
        User user = this.get(user1 -> user1.getDiscord().equals(member));
        if (user != null) {
            user.setName(name, false);
        }
    }

    /**
     * Reloads all users from the config without reloading the config itself.
     * @see UserAssociationService#reload()
     */
    public void reloadUsers() {
        DiscordSync.singleton.getLogger().info("Reloading users...");
        int temp = users.size();
        users.clear();

        for (String key : getUserConfig().getKeys(false)) {
            try {
                long discordId = getUserConfig().isLong(key) ? getUserConfig().getLong(key) : Long.parseLong(getUserConfig().getString(key, "null"));
                if (DiscordSync.singleton.getDiscordAPI().getGuild() == null)
                    throw new NullPointerException("Guild cannot be null");

                users.add(new User(
                        DiscordSync.singleton.getServer().getPlayer(UUID.fromString(key)),
                        DiscordSync.singleton.getDiscordAPI().getGuild().getMemberById(discordId)
                ));
            } catch (Exception e) {
                DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to parse {" + key + ": " + getUserConfig().get(key, "null") + "} to user association.", e);
            }
        }
        DiscordSync.singleton.getLogger().info("OK! " + users.size() + " users loaded. (previously " + temp + ")");
    }

    /**
     * Reloads the <code>users.yml</code> file and updates all {@link User Users}.
     * @see UserAssociationService#reloadUsers()
     */
    public void reload() {
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to reload users.yml.", e);
        }

        this.reloadUsers();
    }

    public ConfigurationSection getUserConfig() {
        return config.isConfigurationSection("user") ? config.getConfigurationSection("user") : config.createSection("user");
    }

    public ConfigurationSection getRequestConfig() {
        return config.isConfigurationSection("request") ? config.getConfigurationSection("request") : config.createSection("request");
    }

    public ConfigurationSection getBlockedSection() {
        return config.isConfigurationSection("blocked") ? config.getConfigurationSection("blocked") : config.createSection("blocked");
    }
}
