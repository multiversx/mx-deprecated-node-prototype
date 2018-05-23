package network.elrond.account;

import network.elrond.core.LRUMap;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.mapdb.Fun;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class AccountsPersistenceUnit<K, V> {

    private static final int MAX_ENTRIES = 10000;

    final LRUMap<K, V> cache = new LRUMap<>(0, MAX_ENTRIES);
    final DB database;
    final ArrayBlockingQueue<Fun.Tuple2<K, V>> queue = new ArrayBlockingQueue<>(10000);


    AccountsPersistenceUnit(String databasePath) throws IOException {
        this.database = initDatabase(databasePath);
    }

    private DB initDatabase(String databasePath) throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        Iq80DBFactory factory = new Iq80DBFactory();
        return factory.open(new File(databasePath), options);
    }

    public void scheduleForPersistence(K key,V val) {
        queue.add(new Fun.Tuple2<>(key, val));
    }


}