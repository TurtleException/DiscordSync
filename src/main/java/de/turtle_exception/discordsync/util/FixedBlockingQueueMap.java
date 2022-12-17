package de.turtle_exception.discordsync.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FixedBlockingQueueMap<K, V> implements Map<K, V> {
    protected final Object lock = new Object();
    protected final K[] keys;
    protected final V[] values;
    protected int cursor;

    public FixedBlockingQueueMap(K[] keys, V[] values) {
        if (keys.length == 0 || values.length == 0)
            throw new IllegalArgumentException("Array may not be of length 0");

        if (keys.length != values.length)
            throw new IllegalArgumentException("Arrays must be of same length");

        this.keys = keys;
        this.values = values;
        this.cursor = 0;
    }

    /* - - - */

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
            for (V v : values)
                if (Objects.equals(v, value))
                    return true;
            return false;
        }
    }

    @Override
    public V get(Object key) {
        synchronized (lock) {
            for (int i = 0; i < keys.length; i++)
                if (Objects.equals(keys[i], key))
                    return values[i];
            return null;
        }
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        synchronized (lock) {
            keys[cursor] = key;
            V val = values[cursor] = value;
            increment();
            return val;
        }
    }

    @Override
    public V remove(Object key) {
        synchronized (lock) {
            for (int i = 0; i < keys.length; i++)
                if (Objects.equals(keys[i], key))
                    return values[i];
            return null;
        }
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        synchronized (lock) {
            for (Entry<? extends K, ? extends V> entry : m.entrySet())
                put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            Arrays.fill(keys, null);
            Arrays.fill(values, null);
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
    public Collection<V> values() {
        ArrayList<V> list = new ArrayList<>(keys.length);
        synchronized (lock) {
            for (int i = 0; i < keys.length; i++)
                list.add(i, values[i]);
        }
        return List.copyOf(list);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        ArrayList<Entry<K, V>> list = new ArrayList<>(keys.length);
        synchronized (lock) {
            for (int i = 0; i < keys.length; i++)
                list.add(i, Map.entry(keys[i], values[i]));
        }
        return Set.copyOf(list);
    }

    protected void increment() {
        synchronized (lock) {
            if (++cursor == values.length)
                cursor = 0;
        }
    }
}
