package de.turtleboi.spigot.dsync.util;

import de.turtleboi.spigot.dsync.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class EntityMap<K, E extends Entity> extends FixedBlockingQueueMap<K, E> {
    public EntityMap(K[] keys, E[] values) {
        super(keys, values);
    }

    public boolean contains(Object o) {
        synchronized (lock) {
            for (K e : keys)
                if (Objects.equals(e, o))
                    return true;
            return false;
        }
    }

    public boolean containsAll(@NotNull Collection<?> c) {
        synchronized (lock) {
            for (Object o : c)
                if (!contains(o))
                    return false;
            return true;
        }
    }

    public boolean containsId(long id) {
        return get(id) != null;
    }

    public E get(long id) {
        synchronized (lock) {
            for (E e : values)
                if (e != null && e.getId() == id)
                    return e;
            return null;
        }
    }
}
