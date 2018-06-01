package network.elrond.account;

import network.elrond.core.LRUMap;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;

/**
 * Abstract implementation of key => value persistence unit
 */
public abstract class AbstractPersistenceUnit<K, V> {

    protected static final int MAX_ENTRIES = 10000;

    protected final String databasePath;
    protected DB database;

    final private LRUMap<K, V> cache = new LRUMap<>(0, MAX_ENTRIES);


    public AbstractPersistenceUnit(String databasePath) throws IOException {
        this.databasePath = databasePath;
        if (databasePath == null || databasePath.isEmpty()) {
            this.database = new MockDB();

        } else {
            this.database = initDatabase(databasePath);
        }
    }

    protected DB initDatabase(String databasePath) throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        Iq80DBFactory factory = new Iq80DBFactory();
        return factory.open(new File(databasePath), options);
    }

    public LRUMap<K, V> getCache() {
        return cache;
    }

    /**
     * Put value on unit
     *
     * @param key
     * @param val
     */
    protected abstract void put(byte[] key, byte[] val);

    /**
     * Get value from unit
     *
     * @param key
     * @return
     */
    protected abstract byte[] get(byte[] key);

    /**
     * Delete everything and recreate unit
     *
     * @throws IOException
     */
    public void recreate() throws IOException {
        destroy();
        this.database = initDatabase(databasePath);

    }

    /**
     * Delete unit
     *
     * @throws IOException
     */
    public void destroy() throws IOException {
        Iq80DBFactory factory = new Iq80DBFactory();
        this.database.close();
        Options options = new Options();
        options.createIfMissing(true);
        factory.destroy(new File(databasePath), options);
    }

    /**
     * Close  unit
     *
     * @throws IOException
     */
    public void close() throws IOException {
        this.database.close();

    }

    /**
     * Clear memory cache
     */
    public void clear() {
        cache.clear();
    }
}
