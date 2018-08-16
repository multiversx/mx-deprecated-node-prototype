package network.elrond.blockchain;

import network.elrond.data.Block;
import network.elrond.data.Receipt;
import network.elrond.data.Transaction;

import java.math.BigInteger;

public enum BlockchainUnitType {
    BLOCK(String.class, Block.class, 50),
    BLOCK_INDEX(BigInteger.class, String.class, 100),
    TRANSACTION(String.class, Transaction.class, 100000),
    SETTINGS(String.class, String.class, 1000),
    RECEIPT(String.class, Receipt.class, 10000),
    TRANSACTION_RECEIPT(String.class, String.class, 10000),
    BLOCK_TRANSACTIONS(String.class, String.class, 10000),;

    private Class<?> keyType;
    private Class<?> valueType;
    private long maxEntries;

    BlockchainUnitType(Class<?> keyType, Class<?> valueType, long maxEntries) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.maxEntries = maxEntries;
    }

    public Class<?> getKeyType() {
        return keyType;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public long getMaxEntries() {
        return maxEntries;
    }
}