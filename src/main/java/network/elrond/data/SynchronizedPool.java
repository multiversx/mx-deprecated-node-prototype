package network.elrond.data;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The SynchronizedPool class is used to control access to the data it holds
 *
 * @param <T> Type of object used in data structures
 * @author Elrond Team - JLS
 * @version 1.0
 * @since 2018-05-16
 */
public class SynchronizedPool<K, T> {
    //main structure to hold data as pool
    private ConcurrentHashMap<K, T> objectPool;

    //array list of hashes of objects to pe fetched from DHT
    private Queue<K> keysToProcess;
    private ReentrantLock lock;

    public SynchronizedPool() {
        objectPool = new ConcurrentHashMap<>();
        keysToProcess = new ArrayDeque<>();
        lock = new ReentrantLock();

    }

    /**
     * Method used to check whether a hash is present or not in main structure data
     *
     * @param key to be checked
     * @return True or False, accordingly
     */
    public boolean isObjectInPool(K key) {
        return (objectPool.contains(key));
    }

    /**
     * Method used to add data
     *
     * @param key hash of the object (eg. ID)
     * @param obj object to be added
     */
    public void addObjectInPool(K key, T obj) {
        objectPool.put(key, obj);
    }

    /**
     * Gets the object pool size
     *
     * @return the size as int
     */
    public int size() {
        return (objectPool.size());
    }

    /**
     * Push hash to the list of need to-be-processed-hashes-list
     *
     * @param key to be added
     */
    public void pushKey(K key) {
        lock.lock();
        try {
            keysToProcess.add(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Return the first hash from the to-be-processed-hashes-list
     *
     * @return the first hash or null if list is empty
     */
    public K popKey() {
        lock.lock();
        try {
            return keysToProcess.poll();
        } finally {
            lock.unlock();
        }
    }

}
