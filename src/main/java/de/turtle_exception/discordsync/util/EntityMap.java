package de.turtle_exception.discordsync.util;

import de.turtle_exception.discordsync.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EntityMap<K, E extends Entity> implements Map<K, E> {
    private final Object lock = new Object();
    private final K[] keys;
    private final E[] values;
    private int cursor;

    public EntityMap(K[] keys, E[] values) {
        if (keys.length == 0 || values.length == 0)
            throw new IllegalArgumentException("Array may not be of length 0");

        if (keys.length != values.length)
            throw new IllegalArgumentException("Arrays must be of same length");

        this.keys = keys;
        this.values = values;
        this.cursor = 0;
    }

    @Override
    public int size() {
        synchronized (lock) {
            int i = 0;
            for (K e : keys)
                if (e != null) i++;
            return i;
        }
    }

    public int capacity() {
        return this.keys.length;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    public boolean contains(Object o) {
        synchronized (lock) {
            for (K e : keys)
                if (Objects.equals(e, o))
                    return true;
            return false;
        }
    }

    @Override
    public E remove(Object o) {
        synchronized (lock) {
            for (int i = 0; i < keys.length; i++) {
                if (Objects.equals(keys[i], o)) {
                    return values[i];
                }
            }
            return null;
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

    @Override
    public void clear() {
        synchronized (lock) {
            Arrays.fill(values, null);
        }
    }

    @Override
    public boolean containsKey(Object key) {
        synchronized (lock) {
            for (K k : keys)
                if (Objects.equals(k, key))
                    return true;
            return false;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        synchronized (lock) {
            for (E e : values)
                if (Objects.equals(e, value))
                    return true;
            return false;
        }
    }

    public boolean containsId(long id) {
        return get(id) != null;
    }

    @Override
    public E get(Object key) {
        synchronized (lock) {
            for (int i = 0; i < keys.length; i++)
                if (Objects.equals(keys[i], key))
                    return values[i];
            return null;
        }
    }

    public E get(long id) {
        synchronized (lock) {
            for (E e : values)
                if (e != null && e.getId() == id)
                    return e;
            return null;
        }
    }

    @Nullable
    @Override
    public E put(K key, E value) {
        synchronized (lock) {
            keys[cursor] = key;
            E val = values[cursor] = value;
            increment();
            return val;
        }
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends E> m) {
        synchronized (lock) {
            for (Entry<? extends K, ? extends E> entry : m.entrySet())
                put(entry.getKey(), entry.getValue());
        }
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        ArrayList<K> list = new ArrayList<>(keys.length);
        synchronized (lock) {
            for (int i = 0; i < keys.length; i++)
                list.add(i, keys[i]);
        }
        return Set.copyOf(list);
    }

    @NotNull
    @Override
    public Collection<E> values() {
        ArrayList<E> list = new ArrayList<>(keys.length);
        synchronized (lock) {
            for (int i = 0; i < keys.length; i++)
                list.add(i, values[i]);
        }
        return List.copyOf(list);
    }

    @NotNull
    @Override
    public Set<Entry<K, E>> entrySet() {
        ArrayList<Entry<K, E>> list = new ArrayList<>(keys.length);
        synchronized (lock) {
            for (int i = 0; i < keys.length; i++)
                list.add(i, Map.entry(keys[i], values[i]));
        }
        return Set.copyOf(list);
    }

    public E remove(int index) {
        synchronized (lock) {
            keys[index] = null;
            E val = values[index];
            values[index] = null;
            return val;
        }
    }

    private E removeCurrent() {
        synchronized (lock) {
            keys[cursor] = null;
            E val = values[cursor];
            values[cursor] = null;
            return val;
        }
    }

    public E get(int i) {
        synchronized (lock) {
            return values[i];
        }
    }

    private void increment() {
        synchronized (lock) {
            if (++cursor == values.length)
                cursor = 0;
        }
    }
}
