package network.elrond.blockchain;

import network.elrond.account.AbstractPersistenceUnit;

import java.io.IOException;

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