package network.elrond.blockchain;

import network.elrond.core.LRUMap;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;

public class BlockchainPersistenceUnit<K, V> {

    private static final int MAX_ENTRIES = 10000;

    final LRUMap<K, V> cache = new LRUMap<>(0, MAX_ENTRIES);
    final DB database;
    final Class<V> clazz;


    BlockchainPersistenceUnit(String databasePath, Class<V> clazz) throws IOException {

        this.database = initDatabase(databasePath);

        this.clazz = clazz;
    }

    private DB initDatabase(String databasePath) throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        Iq80DBFactory factory = new Iq80DBFactory();
        return factory.open(new File(databasePath), options);
    }

}