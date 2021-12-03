package de.eldritch.spigot.DiscordSync.util;

import java.util.HashMap;

// TODO: remove
public class BiMap<K, V> {
    private final HashMap<K, V> map = new HashMap<>();
    private final HashMap<V, K> inv = new HashMap<>();

    public void put(K key, V value) {
        map.put(key, value);
        inv.put(value, key);
    }

    public V getValue(K key) {
        return map.get(key);
    }

    public K getKey(V value) {
        return inv.get(value);
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public boolean containsValue(V value) {
        return inv.containsKey(value);
    }

    public boolean removeKey(K key) {
        V value = map.remove(key);
        return inv.remove(value, key);
    }

    public boolean removeValue(V value) {
        K key = inv.remove(value);
        return map.remove(key, value);
    }

    public void clear() {
        map.clear();
        inv.clear();
    }
}
