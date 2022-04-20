package de.eldritch.spigot.discord_sync.sync;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.entities.DiscordMessage;
import de.eldritch.spigot.discord_sync.entities.Message;
import de.eldritch.spigot.discord_sync.entities.interfaces.DiscordSynchronizable;
import de.eldritch.spigot.discord_sync.entities.interfaces.MinecraftSynchronizable;
import de.eldritch.spigot.discord_sync.entities.interfaces.Referencable;
import de.eldritch.spigot.discord_sync.entities.interfaces.Synchronizable;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

public class SynchronizationService {
    /**
     * Maximum capacity of the message cache.
     * @see SynchronizationService#referencableCache
     */
    private static final int CACHE_CAPACITY = 256;
    /**
     * Cache with a capacity defined by {@link SynchronizationService#CACHE_CAPACITY CACHE_CAPACITY} that contains the
     * most recent {@link Message messages}. Every message has a {@link String} key that represents a number with 4
     * digits used to easily reference these messages and reply to them. The reference numbers are stored as a String to
     * prevent trimming of leading zeros and to allow possible extra reference Strings that contain letters.
     */
    private final LinkedHashMap<String, Referencable> referencableCache = new LinkedHashMap<>(CACHE_CAPACITY) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Referencable> eldest) {
            return size() >= CACHE_CAPACITY;
        }
    };

    /**
     * The bound parameter for quick reference numbers. This number is the higher power of 10 for the
     * {@link SynchronizationService#CACHE_CAPACITY} so all messages in the cache can be uniquely referenced while
     * keeping the length of reference numbers as short as possible.
     * @see SynchronizationService#newRefNum()
     * @see SynchronizationService#refNum
     */
    private static final int QUICK_REFERENCE_NUMBER_BOUND = (int) Math.pow(10, (int) Math.log10(CACHE_CAPACITY) + 1);
    /**
     * The current quick reference number. This will be incremented with every new message that is cached. Once it has
     * reached the highest possible number defined by {@link SynchronizationService#QUICK_REFERENCE_NUMBER_BOUND} it
     * will be set to 0 and begin incrementing again.
     * <p>The initial value is a pseudo random number within the bounds of 0 and the defined maximum.
     * @see SynchronizationService#newRefNum()
     */
    private int refNum = new Random().nextInt(0, QUICK_REFERENCE_NUMBER_BOUND);


    /**
     * Static helper-method to avoid using <code>DiscordSync.singleton.getSynchronizationService()</code> as prefix
     * every time.
     * @param obj A Synchronizable to handle.
     * @see SynchronizationService#handle0(Synchronizable)
     */
    public static void handle(Synchronizable obj) {
        DiscordSync.singleton.getSynchronizationService().handle0(obj);
    }

    public void handle0(Synchronizable obj) {
        if (obj instanceof Referencable ref) {
            final String refNum = newRefNum();
            referencableCache.put(refNum, ref);
            ref.setRefNum(refNum);
        }

        if (obj instanceof Message msg)
            DiscordSync.singleton.getLogger().logrb(Level.INFO, null, msg.getLogMessage());

        if (obj instanceof MinecraftSynchronizable mSync)
            mSync.sendToMinecraft();

        if (obj instanceof DiscordSynchronizable dSync)
            dSync.sendToDiscord();
    }

    /**
     * Provides a new quick reference number.
     * @see SynchronizationService#QUICK_REFERENCE_NUMBER_BOUND
     * @see SynchronizationService#refNum
     */
    private String newRefNum() {
        if (++refNum >= QUICK_REFERENCE_NUMBER_BOUND)
            refNum = 0;

        return new DecimalFormat(String.valueOf(QUICK_REFERENCE_NUMBER_BOUND).substring(1)).format(refNum);
    }

    public @Nullable Referencable getCachedReferencable(String refNumber) {
        return referencableCache.get(refNumber);
    }

    /**
     * Provides a cached {@link Referencable} that either originated from Discord or is a {@link DiscordSynchronizable}
     * with its Discord message representation matching the given snowflake ID or <code>null</code> if no such message
     * could be found in the cache.
     * @param snowflake Snowflake ID of the Discord message representation.
     * @return Message or <code>null</code>.
     */
    public @Nullable Referencable getCachedReferencableBySnowflake(long snowflake) {
        for (Referencable cachedReferencable : referencableCache.values()) {
            if (cachedReferencable instanceof DiscordMessage dMsg) {
                if (dMsg.getSnowflake() == snowflake)
                    return dMsg;
            }

            if (cachedReferencable instanceof DiscordSynchronizable dSync) {
                try {
                    if (dSync.getSnowflake() == snowflake)
                        return cachedReferencable;
                } catch (IllegalStateException ignored) { }
            }
        }
        return null;
    }

    /**
     * Provides a cached {@link Referencable} that matches the given TurtleID or <code>null</code> if no such message
     * could be found in the cache.
     * @param turtle ID of the message.
     * @return Message or <code>null</code>.
     */
    public @Nullable Referencable getCachedReferencableByTurtle(long turtle) {
        for (Referencable cachedMsg : referencableCache.values())
            if (cachedMsg.getID() == turtle)
                return cachedMsg;
        return null;
    }
}
