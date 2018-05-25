package network.elrond.blockchain;

import network.elrond.data.DataBlock;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PConnection;
import org.iq80.leveldb.DB;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class Blockchain implements Serializable {

    private final BlockchainContext context;

    private final Map<BlockchainUnitType, BlockchainPersistenceUnit<?, ?>> blockchain = new HashMap<>();

    public Blockchain(BlockchainContext context) throws IOException {
        this.context = context;

        String blockPath = context.getDatabasePath(BlockchainUnitType.BLOCK);
        blockchain.put(BlockchainUnitType.BLOCK,
                new BlockchainPersistenceUnit<String, DataBlock>(blockPath, DataBlock.class));

        String indexPath = context.getDatabasePath(BlockchainUnitType.BLOCK_INDEX);
        blockchain.put(BlockchainUnitType.BLOCK_INDEX,
                new BlockchainPersistenceUnit<BigInteger, String>(indexPath, String.class));


        String transactionsPath = context.getDatabasePath(BlockchainUnitType.TRANSACTION);
        blockchain.put(BlockchainUnitType.TRANSACTION,
                new BlockchainPersistenceUnit<String, Transaction>(transactionsPath, Transaction.class));

        String settingsPath = context.getDatabasePath(BlockchainUnitType.SETTINGS);
        blockchain.put(BlockchainUnitType.SETTINGS,
                new BlockchainPersistenceUnit<String, String>(settingsPath, String.class));
    }

    public <H extends Object, B> BlockchainPersistenceUnit<H, B> getUnit(BlockchainUnitType type) {
        return (BlockchainPersistenceUnit<H, B>) blockchain.get(type);
    }

    public <H extends Object, B> Class<B> getClazz(BlockchainUnitType type) {
        BlockchainPersistenceUnit<H, B> unit = getUnit(type);
        return unit.clazz;
    }

    public BlockchainContext getContext() {
        return context;
    }

    public P2PConnection getConnection() {
        return context.getConnection();
    }

    public void flush() {
        for (BlockchainUnitType key : blockchain.keySet()) {
            blockchain.get(key).getCache().clear();
        }

    }
}
