package network.elrond.blockchain;

import network.elrond.data.BaseBlockchainTest;
import network.elrond.data.Block;
import network.elrond.data.SerializationService;
import network.elrond.p2p.P2PConnection;
import network.elrond.service.AppServiceProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class BlockchainTest extends BaseBlockchainTest {

    private SerializationService serializationService = AppServiceProvider.getSerializationService();
    private Blockchain blockchain;
    List<BlockchainUnitType> blockchainUnitTypes = Arrays.asList(
            BlockchainUnitType.BLOCK,
            BlockchainUnitType.BLOCK_INDEX,
            BlockchainUnitType.TRANSACTION,
            BlockchainUnitType.SETTINGS)
            ;

    @Before
    public void setUp() throws IOException {


        if (blockchain != null) {
            return;
        }

        blockchain = new Blockchain(getDefaultTestBlockchainContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockchainFromNullContextShouldThrowException() throws IOException {
        Blockchain b = new Blockchain(null);
    }

    @Test
    public void testBlockchainConstructor() throws IOException {
        Blockchain b = new Blockchain(new BlockchainContext());
        Assert.assertNotNull(b);
    }

    @Test
    public void testGetUnitForBLOCK() throws IOException {
        BlockchainPersistenceUnit unit = blockchain.getUnit(BlockchainUnitType.BLOCK);
        Assert.assertNotNull(unit);
    }

    @Test
    public void testGetUnitForBLOCK_INDEX() throws IOException {
        BlockchainPersistenceUnit unit = blockchain.getUnit(BlockchainUnitType.BLOCK_INDEX);
        Assert.assertNotNull(unit);
    }
    @Test
    public void testGetUnitForTRANSACTION() throws IOException {
        BlockchainPersistenceUnit unit = blockchain.getUnit(BlockchainUnitType.TRANSACTION);
        Assert.assertNotNull(unit);
    }
    @Test
    public void testGetUnitForSETTINGS() throws IOException {
        BlockchainPersistenceUnit unit = blockchain.getUnit(BlockchainUnitType.SETTINGS);
        Assert.assertNotNull(unit);
    }

    @Test
    public void testGetContext() throws IOException {
        BlockchainContext context = mock(BlockchainContext.class);
        Blockchain blockchain = new Blockchain(context);
        Assert.assertEquals(context, blockchain.getContext());
    }

    @Test
    public void testGetConnection() throws IOException {
        BlockchainContext context = mock(BlockchainContext.class);
        P2PConnection p2PConnection = mock(P2PConnection.class);
        when(context.getConnection()).thenReturn(p2PConnection);
        Blockchain blockchain = new Blockchain(context);
        Assert.assertEquals(p2PConnection, blockchain.getConnection());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCurrentBlockWithNullShouldThrowException() throws IOException {
        blockchain.setCurrentBlock(null);
    }

    @Test
    public void testSetCurrentBlock() throws IOException {
        Block block = mock(Block.class);
        blockchain.setCurrentBlock(block);
        Assert.assertEquals(block, blockchain.getCurrentBlock());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetGenesisBlockWithNullShouldThrowException() throws IOException {
        blockchain.setGenesisBlock(null);
    }

    @Test
    public void testSetGenesisBlock() throws IOException {
        Block block = mock(Block.class);
        blockchain.setGenesisBlock(block);
        Assert.assertEquals(block, blockchain.getGenesisBlock());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCurrentBlockIndexWithNullShouldThrowException() throws IOException {
        blockchain.setCurrentBlockIndex(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCurrentBlockIndexWithNegativeShouldThrowException() throws IOException {
        blockchain.setCurrentBlockIndex(BigInteger.valueOf(-2));
    }

    @Test
    public void testSetCurrentBlockIndex() throws IOException {
        BigInteger bigInteger = BigInteger.valueOf(5);
        blockchain.setCurrentBlockIndex(bigInteger);
        Assert.assertEquals(bigInteger, blockchain.getCurrentBlockIndex());
    }

    @Test
    public void testStopPersistenceUnit() throws IOException {
        CustomTestBlockchain chain = new CustomTestBlockchain(new BlockchainContext());
        chain.stopPersistenceUnit();
        for(BlockchainUnitType type : blockchainUnitTypes)
        {
            BlockchainPersistenceUnit persistenceUnit = chain.getUnit(type);
            verify(persistenceUnit,  times(1)).close();
        }
    }


}
