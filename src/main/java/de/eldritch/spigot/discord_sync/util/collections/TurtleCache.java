package de.eldritch.spigot.discord_sync.util.collections;

import de.eldritch.spigot.discord_sync.entities.interfaces.Turtle;
import org.jetbrains.annotations.Nullable;

public class TurtleCache<E extends Turtle> extends LimitedCache<E> {
    public TurtleCache(int capacity) {
        super(capacity);
    }

    public @Nullable E get(long turtle) {
        synchronized (lock) {
            for (E e : this)
                if (e.getID() == turtle)
                    return e;
        }
        return null;
    }
}
