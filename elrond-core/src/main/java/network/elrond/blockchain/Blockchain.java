package network.elrond.blockchain;

import network.elrond.account.AbstractPersistenceUnit;
import network.elrond.core.Util;
import network.elrond.data.Block;
import network.elrond.p2p.P2PConnection;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class Blockchain implements Serializable, PersistenceUnitContainer {

    private static final Logger logger = LogManager.getLogger(Blockchain.class);

    protected final BlockchainContext context;

    protected Block currentBlock;
    protected BigInteger currentBlockIndex = BigInteger.valueOf(-1);
    protected Block genesisBlock;

    protected TransactionsPool pool = new TransactionsPool();


    protected final Map<BlockchainUnitType, BlockchainPersistenceUnit<?, ?>> blockchain = new HashMap<>();

    private BigInteger networkBlockHeight = Util.BIG_INT_MIN_ONE;

    public Blockchain(BlockchainContext context) throws IOException {
        Util.check(context != null, "context!=null");
        this.context = context;

        generatePersistenceUnitMap(context);
    }

    public void generatePersistenceUnitMap(BlockchainContext context) throws IOException {
        for (BlockchainUnitType type : BlockchainUnitType.values()) {
            String path = context.getDatabasePath(type);
            Class<?> ketType = type.getKeyType();
            Class<?> valueType = type.getValueType();
            BlockchainPersistenceUnit<?, ?> unit = new BlockchainPersistenceUnit<>(path, valueType);
            blockchain.put(type, unit);
        }
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
        Util.check(currentBlock != null, "currentBlock!=null");
        this.currentBlock = currentBlock;
        logger.traceExit();
    }

    public Block getGenesisBlock() {
        return genesisBlock;
    }

    public void setGenesisBlock(Block genesisBlock) {
        Util.check(genesisBlock != null, "genesisBlock!=null");
        this.genesisBlock = genesisBlock;
        logger.traceExit();
    }

    public BigInteger getCurrentBlockIndex() {
        return currentBlockIndex;
    }

    public void setCurrentBlockIndex(BigInteger currentBlockIndex) {
        Util.check(currentBlockIndex!=null, "currentBlockIndex!=null");
        Util.check(currentBlockIndex.compareTo(BigInteger.ZERO) >= 0, "currentBlockIndex!=null");
        this.currentBlockIndex = currentBlockIndex;
    }

    public Shard getShard() {
        return context.getShard();
    }

    public void flush() {
        logger.traceEntry();
        for (BlockchainUnitType key : blockchain.keySet()) {
            blockchain.get(key).clear();
        }
        logger.traceExit();
    }

    @Override
    public void stopPersistenceUnit() {
        logger.traceEntry();
        for (AbstractPersistenceUnit<?, ?> unit : blockchain.values()) {
            try {
                unit.close();
            } catch (IOException e) {
                logger.catching(e);
            }
        }
        logger.traceExit();
    }

    public TransactionsPool getPool() {
        return pool;
    }

    public BigInteger getNetworkHeight(){
        return networkBlockHeight;
    }

    public synchronized void setNetworkHeight(BigInteger networkBlockHeight){
        this.networkBlockHeight = networkBlockHeight;
    }

    @Override
    public String toString() {
        return String.format("Current block: %s", ((currentBlock != null) ? currentBlock.toString() : "No block"));
    }
}
