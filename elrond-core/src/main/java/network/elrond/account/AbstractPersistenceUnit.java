package network.elrond.account;

import network.elrond.core.LRUMap;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.mapdb.Fun;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class AbstractPersistenceUnit<K, V> {

    private static final int MAX_ENTRIES = 10000;

    final protected LRUMap<K, V> cache = new LRUMap<>(0, MAX_ENTRIES);
    protected DB database;
    final ArrayBlockingQueue<Fun.Tuple2<K, V>> queue = new ArrayBlockingQueue<>(10000);

    private final String databasePath;

    public AbstractPersistenceUnit(String databasePath) throws IOException {
        if (databasePath == null || databasePath.isEmpty()) {
            this.databasePath = null;
            this.database = null;
        } else {
            this.databasePath = databasePath;
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

    public void scheduleForPersistence(K key, V val) {
        queue.add(new Fun.Tuple2<>(key, val));
    }

    public abstract void put(byte[] key, byte[] val);

    public abstract byte[] get(byte[] key);

    public void destroyAndReCreate() throws IOException{
        if (databasePath != null) {
            destroy();

            this.database = initDatabase(databasePath);
        }
    }

    public void destroy() throws IOException{
        if (databasePath != null) {
            Iq80DBFactory factory = new Iq80DBFactory();
            this.database.close();
            Options options = new Options();
            options.createIfMissing(true);
            factory.destroy(new File(databasePath), options);
        }
    }

    public void close() throws IOException{
        if (database != null) {
            this.database.close();
        }
    }
}
