package de.turtleboi.spigot.dsync.util;

import de.turtleboi.spigot.dsync.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * A simple set containing Entities. The set is backed by a {@link ConcurrentHashMap} and values are mapped to their
 * unique id.
 * <p> This implementation should be thread-safe.
 * @see Entity#getId()
 */
public class EntitySet<E extends Entity> implements Set<E> {
    private final Object lock = new Object();
    private final ConcurrentHashMap<Long, E> content = new ConcurrentHashMap<>();

    /** Returns the value with the specified id, or {@code null} if this set does not contain that value. */
    public @Nullable E get(long id) {
        synchronized (lock) {
            return content.get(id);
        }
    }

    @SuppressWarnings("unchecked")
    public <E1> @Nullable E1 get(long id, Class<E1> type) {
        E val = this.get(id);
        if (type.isInstance(val))
            return (E1) val;
        return null;
    }

    @Override
    public int size() {
        synchronized (lock) {
            return content.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return content.isEmpty();
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (lock) {
            if (o instanceof Entity entity) {
                return content.get(entity.getId()) != null;
            }
            return false;
        }
    }

    public boolean containsId(long id) {
        synchronized (lock) {
            return content.get(id) != null;
        }
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        synchronized (lock) {
            return content.values().iterator();
        }
    }

    @Override
    public Object @NotNull [] toArray() {
        synchronized (lock) {
            return content.values().toArray();
        }
    }

    @Override
    @SuppressWarnings({"SuspiciousToArrayCall", "NullableProblems"})
    public <T> @NotNull T[] toArray(T[] a) {
        synchronized (lock) {
            return content.values().toArray(a);
        }
    }

    @Override
    public boolean add(E e) {
        synchronized (lock) {
            // comparing the old and new value is not necessary because the id should be unique to this object.
            return content.put(e.getId(), e) == null;
        }
    }

    public @Nullable E put(@NotNull E e) {
        synchronized (lock) {
            return content.put(e.getId(), e);
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (lock) {
            return content.remove(o) != null;
        }
    }

    public E removeById(long id) {
        synchronized (lock) {
            return content.remove(id);
        }
    }

    public E removeById(@NotNull String str) throws NumberFormatException {
        synchronized (lock) {
            return this.removeById(Long.parseLong(str));
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        synchronized (lock) {
            return content.values().containsAll(c);
        }
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        synchronized (lock) {
            boolean b = false;
            for (E e : c)
                b = this.add(e) || b;
            return b;
        }
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        synchronized (lock) {
            boolean b = false;
            for (E e : content.values())
                if (!c.contains(e))
                    b = this.remove(e) || b;
            return b;
        }
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        synchronized (lock) {
            boolean b = false;
            for (Object e : c)
                b = this.remove(e) || b;
            return b;
        }
    }

    public int removeAll(@NotNull Predicate<E> predicate) {
        synchronized (lock) {
            int i = 0;
            for (E value : content.values()) {
                if (predicate.test(value)) {
                    content.remove(value.getId(), value);
                    i++;
                }
            }
            return i;
        }
    }

    public int removeAll(@NotNull Class<? extends E> type) {
        return this.removeAll(type::isInstance);
    }

    @Override
    public void clear() {
        synchronized (lock) {
            content.clear();
        }
    }
}
