package network.elrond.blockchain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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
    public void testPutWithNullHashShouldThrowExtension() throws IOException {
        blockchainService.put(null, "test", blockchain, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullObjectShouldThrowExtension() throws IOException {
        blockchainService.put("testHash",  null, blockchain, BlockchainUnitType.BLOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullBlockchainShouldThrowExtension() throws IOException {
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
        List objList = blockchainService.getAll(Arrays.asList(testHash, testHash2), blockchain, BlockchainUnitType.BLOCK);
        Assert.assertEquals(2, objList.size());
        Assert.assertEquals(testObject1, objList.get(0));
        Assert.assertEquals(testObject2, objList.get(1));
    }


}


//    public synchronized <H extends Object, B extends Serializable> boolean contains(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException {
//        logger.traceEntry("params: {} {} {}", hash, blockchain, type);
//        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);
//        P2PConnection connection = blockchain.getConnection();
//        LRUMap<H, B> cache = unit.getCache();
//
//        if (cache.contains(hash)) {
//            logger.trace("cache contains hash");
//            return logger.traceExit(true);
//        }
//        B block = get(hash, blockchain, type);
//        return logger.traceExit(block != null);
//    }
//
//
//    /**
//     * Put object on object chain (memory->database->network)
//     *
//     * @param hash   object hash
//     * @param object
//     * @throws IOException
//     */
//    @Override
//    public synchronized <H extends Object, B extends Serializable> void put(H hash, B object, Blockchain blockchain, BlockchainUnitType type) throws IOException {
//        logger.traceEntry("params: {} {} {} {}", hash, object, blockchain, type);
//
//        if (object == null || hash == null) {
//            logger.trace("object or hash is null");
//            logger.traceExit();
//            return;
//        }
//
//        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);
//        P2PConnection connection = blockchain.getConnection();
//
//        unit.getCache().put(hash, object);
//        String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(object);
//        unit.put(bytes(hash.toString()), bytes(strJSONData));
//
//        logger.trace("Locally stored!");
//
//        if (!isOffline(connection)) {
//            AppServiceProvider.getP2PObjectService().put(connection, hash.toString(), object);
//            logger.trace("DHT stored!");
//        }
//
//        logger.traceExit();
//    }
//
//    @Override
//    public <H extends Object, B extends Serializable> List<B> getAll(List<H> hashes, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException {
//        logger.traceEntry("params: {} {} {}", hashes, blockchain, type);
//        List<B> list = new ArrayList<>();
//
//        for (H hash : hashes) {
//            list.add(get(hash, blockchain, type));
//        }
//
//        return logger.traceExit(list);
//    }
//
//    /**
//     * Get block form blockchain (memory->database->network)
//     *
//     * @param hash block hash
//     * @return
//     * @throws IOException
//     * @throws ClassNotFoundException
//     */
//    @Override
//    public synchronized <H extends Object, B extends Serializable> B get(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException {
//        logger.traceEntry("params: {} {} {}", hash, blockchain, type);
//
//        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);
//        P2PConnection connection = blockchain.getConnection();
//
//        LRUMap<H, B> cache = unit.getCache();
//
//        boolean exists = cache.get(hash) != null;
//        if (!exists) {
//            B object = getDataFromDatabase(hash, unit);
//            if (object == null) {
//                logger.trace("Getting from DHT...");
//                object = getDataFromNetwork(hash, unit, connection);
//            }
//
//            if (object != null) {
//                cache.put(hash, object);
//                String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(object);
//                unit.put(bytes(hash.toString()), bytes(strJSONData));
//                logger.trace("Got from local storace, placed on DHT!");
//            }
//        }
//
//        B result = cache.get(hash);
//        return logger.traceExit(result);
//
//    }
//
//    private <H extends Object, B extends Serializable> B getDataFromNetwork(H hash, BlockchainPersistenceUnit<H, B> unit, P2PConnection connection)
//            throws ClassNotFoundException, IOException {
//        logger.traceEntry("params: {} {} {}", hash, unit, connection);
//
//        if (isOffline(connection)) {
//            logger.trace("offline!");
//            logger.traceExit();
//            return null;
//        }
//        return AppServiceProvider.getP2PObjectService().get(connection, hash.toString(), unit.clazz);
//    }
//
//    private <B extends Serializable, H extends Object> B getDataFromDatabase(H hash, BlockchainPersistenceUnit<H, B> unit) {
//        logger.traceEntry("params: {} {}", hash, unit);
//        byte[] data = unit.get(bytes(hash.toString()));
//        if (data == null) {
//            logger.trace("data do not exists!");
//            logger.traceExit();
//            return null;
//        }
//
//        String strJSONData = asString(data);
//        return logger.traceExit(decodeObject(unit.clazz, strJSONData));
//    }
//
//    private <B extends Serializable> B decodeObject(Class<B> clazz, String strJSONData) {
//        logger.traceEntry("params: {} {}", clazz, strJSONData);
//        if (strJSONData == null) {
//            logger.trace("strJSONData is null");
//            logger.traceExit();
//            return null;
//        }
//        return logger.traceExit(AppServiceProvider.getSerializationService().decodeJSON(strJSONData, clazz));
//    }
//
//    protected boolean isOffline(P2PConnection connection) {
//        return connection == null;
//    }
//
//}
