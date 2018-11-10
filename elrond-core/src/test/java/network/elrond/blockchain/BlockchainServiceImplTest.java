package network.elrond.blockchain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

public class BlockchainServiceImplTest {
    private BlockchainService blockchainService;
    private Blockchain blockchain;

    @Before
    public void SetUp(){
        blockchainService = new BlockchainServiceImpl();
        blockchain = mock(Blockchain.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsWithNullHashShouldThrowExtension() throws IOException, ClassNotFoundException {
        blockchainService.contains(null, blockchain, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsWithNullBlockchainShouldThrowExtension() throws IOException, ClassNotFoundException {
        blockchainService.contains("testHash", null, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullHashShouldThrowExtension() throws IOException, ClassNotFoundException {
        blockchainService.put(null, "test", blockchain, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullObjectShouldThrowExtension() throws IOException, ClassNotFoundException {
        blockchainService.put("testHash",  null, blockchain, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullBlockchainShouldThrowExtension() throws IOException, ClassNotFoundException {
        blockchainService.put("testHash",  "test", null, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithNullHashShouldThrowExtension() throws IOException, ClassNotFoundException {
        blockchainService.get(null, blockchain, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithNullBlockchainShouldThrowExtension() throws IOException, ClassNotFoundException {
        blockchainService.get("testHash", null, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllWithNullHashShouldThrowExtension() throws IOException, ClassNotFoundException {
        blockchainService.getAll(null, blockchain, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllWithNullBlockchainShouldThrowExtension() throws IOException, ClassNotFoundException {
        blockchainService.getAll(new ArrayList<String>(), null, BlockchainUnitType.BLOCK);
    }

    @Test
    public void testPut() throws IOException, ClassNotFoundException {
        String testHash = "testHash";
        String testObject = "testObject";
        Blockchain blockchain = new Blockchain(new BlockchainContext());
        Assert.assertNull(blockchain.getUnit(BlockchainUnitType.BLOCK).getCache().get(testHash));
        blockchainService.put(testHash,  testObject, blockchain, BlockchainUnitType.BLOCK);
        Assert.assertNotNull(blockchain.getUnit(BlockchainUnitType.BLOCK).getCache().get(testHash));
        String str = blockchainService.get(testHash, blockchain, BlockchainUnitType.BLOCK);
        Assert.assertNotNull(str);
        Assert.assertEquals(testObject, str);
    }

    @Test
    public void testPutAndGet() throws IOException, ClassNotFoundException {
        String testHash = "testHash";
        String testObject = "testObject";
        Blockchain blockchain = new Blockchain(new BlockchainContext());
        blockchainService.put(testHash,  testObject, blockchain, BlockchainUnitType.BLOCK);
        String str = blockchainService.get(testHash, blockchain, BlockchainUnitType.BLOCK);
        Assert.assertNotNull(str);
        Assert.assertEquals(testObject, str);
    }

    @Test
    public void testPutAndGetAll() throws IOException, ClassNotFoundException {
        String testHash = "testHash1";
        String testHash2 = "testHash2";
        String testObject1 = "testObject";
        String testObject2 = "testObject2";
        Blockchain blockchain = new Blockchain(new BlockchainContext());
        blockchainService.put(testHash,  testObject1, blockchain, BlockchainUnitType.BLOCK);
        blockchainService.put(testHash2,  testObject2, blockchain, BlockchainUnitType.BLOCK);
        List<Serializable> objList = blockchainService.getAll(Arrays.asList(testHash, testHash2), blockchain, BlockchainUnitType.BLOCK);
        Assert.assertEquals(2, objList.size());
        Assert.assertEquals(testObject1, objList.get(0));
        Assert.assertEquals(testObject2, objList.get(1));
    }

}