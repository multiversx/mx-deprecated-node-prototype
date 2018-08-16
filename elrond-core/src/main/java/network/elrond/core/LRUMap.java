package network.elrond.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Simple LRU map used for reusing lookup values.
 */
public class LRUMap<K, V> {
    private Cache<K, V> cache;
    protected final int maxEntries;

    public LRUMap(int initialEntries, int maxEntries) {
        this.maxEntries = maxEntries;
        cache = CacheBuilder.newBuilder().maximumSize(maxEntries).build();
    }


    public V get(K key) {
        return cache.getIfPresent(key);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public boolean contains(K key) {
        return this.get(key) != null;
    }

    public void clear() {
        cache.cleanUp();
    }
}