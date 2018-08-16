package network.elrond.blockchain;

import network.elrond.account.AbstractPersistenceUnit;
import network.elrond.core.Util;

import java.io.IOException;

public class BlockchainPersistenceUnit<K, V> extends AbstractPersistenceUnit<K, V> {

    final Class<V> clazz;

    BlockchainPersistenceUnit(String databasePath, Class<V> clazz, long maxEntries) throws IOException {
        super(databasePath, maxEntries);
        this.clazz = clazz;
    }

    @Override
    public void put(byte[] key, byte[] val) {
        Util.check(key!=null, "key!=null");
        Util.check(val!=null, "val!=null");
        database.put(key, val);
    }

    @Override
    public byte[] get(byte[] key) {
        return database.get(key);
    }
}