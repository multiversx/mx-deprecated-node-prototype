package network.elrond.blockchain;

import network.elrond.account.AbstractPersistenceUnit;
import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.data.DataBlock;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PConnection;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class Blockchain implements Serializable, PersistenceUnitContainer {

    private final BlockchainContext context;

    private Block currentBlock;
    private BigInteger currentBlockIndex = BigInteger.valueOf(-1);

    private Block genesisBlock;

    private final Map<BlockchainUnitType, BlockchainPersistenceUnit<?, ?>> blockchain = new HashMap<>();

    public Blockchain(BlockchainContext context) throws IOException {
        Util.check(context!=null, "context!=null");
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

    public Block getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(Block currentBlock) {
        Util.check(currentBlock!=null, "currentBlock!=null");
        this.currentBlock = currentBlock;
    }

    public Block getGenesisBlock(){
        return(genesisBlock);
    }

    public void setGenesisBlock(Block genesisBlock){
        Util.check(genesisBlock!=null, "genesisBlock!=null");
        this.genesisBlock = genesisBlock;
    }

    public BigInteger getCurrentBlockIndex() {
        return currentBlockIndex;
    }

    public void setCurrentBlockIndex(BigInteger currentBlockIndex) {
        this.currentBlockIndex = currentBlockIndex;
    }

    public void flush() {
        for (BlockchainUnitType key : blockchain.keySet()) {
            blockchain.get(key).clear();
        }
    }

    @Override
    public void stopPersistenceUnit() {
        for (AbstractPersistenceUnit<?, ?> unit : blockchain.values()) {
            try {
                unit.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "Current block: " + ((currentBlock != null) ? currentBlock.toString() : "No block");
    }
}
