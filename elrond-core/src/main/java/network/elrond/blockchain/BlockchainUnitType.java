package network.elrond.blockchain;

import network.elrond.data.Block;
import network.elrond.data.Receipt;
import network.elrond.data.Transaction;

import java.math.BigInteger;

public enum BlockchainUnitType {
    BLOCK(String.class, Block.class),
    BLOCK_INDEX(BigInteger.class, String.class),
    TRANSACTION(String.class, Transaction.class),
    SETTINGS(String.class, String.class),
    RECEIPT(String.class, Receipt.class),
    TRANSACTION_RECEIPT(String.class, String.class),
    ;

    private Class<?> keyType;
    private Class<?> valueType;

    BlockchainUnitType(Class<?> keyType, Class<?> valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public Class<?> getKeyType() {
        return keyType;
    }

    public Class<?> getValueType() {
        return valueType;
    }
}