package network.elrond.blockchain;

import network.elrond.account.AbstractPersistenceUnit;
import network.elrond.data.Block;
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
        this.context = context;

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
        if (currentBlock == null) {
            throw new IllegalArgumentException("CurrentBlock cannot be null");
        }
        this.currentBlock = currentBlock;
    }

    public Block getGenesisBlock() {
        return (genesisBlock);
    }

    public void setGenesisBlock(Block genesisBlock) {
        if (genesisBlock == null) {
            throw new IllegalArgumentException("GenesisBlock cannot be null");
        }
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
