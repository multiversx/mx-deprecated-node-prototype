package network.elrond.blockchain;

import network.elrond.account.AbstractPersistenceUnit;
import network.elrond.core.LRUMap;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class BlockchainPersistenceUnit<K, V> extends AbstractPersistenceUnit<K, V> {

    final Class<V> clazz;

    BlockchainPersistenceUnit(String databasePath, Class<V> clazz) throws IOException {

        super(databasePath);
        this.clazz = clazz;
    }

    @Override
    public void put(byte[] key, byte[] val) {
        database.put(key, val);
    }

    @Override
    public byte[] get(byte[] key) {
        return database.get(key);
    }
}