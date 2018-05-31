package network.elrond.blockchain;

import network.elrond.data.BaseBlockchainTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

public class BlockchainPersistenceUnitTest {
    Random r;

    @Before
    public void setUp() {
        r = new Random();
    }

    @After
    public void TearDown() throws IOException, InterruptedException {
    }

    //@Test(expected = IllegalArgumentException.class)
    public void testBlockchainPersistenceUnitWithNullPathShouldThrowException() throws IOException {
        BlockchainPersistenceUnit<String, String> blockchainPersistenceUnit =
                new BlockchainPersistenceUnit<>((String) null, String.class);
        blockchainPersistenceUnit.close();
    }

    @Test
    public void testPutNullObjectShouldThrowException() throws IOException {
        BlockchainPersistenceUnit<String, String> blockchainPersistenceUnit =
                new BlockchainPersistenceUnit<>(BaseBlockchainTest.BLOCKCHAIN_BLOCK_DATA_TEST_PATH, String.class);
        byte[] key = new byte[] { (byte)r.nextInt()};
        byte[] value = new byte[] { (byte)r.nextInt()};
         blockchainPersistenceUnit.put(key,value);
         byte[] readValue = blockchainPersistenceUnit.get(key);
        Assert.assertArrayEquals(value, readValue);
        blockchainPersistenceUnit.close();
    }

    @Test
    public void testDestroyAndRecreate() throws IOException {
        BlockchainPersistenceUnit<String, String> blockchainPersistenceUnit =
                new BlockchainPersistenceUnit<>(BaseBlockchainTest.BLOCKCHAIN_BLOCK_DATA_TEST_PATH, String.class);

        byte[] key = new byte[]{(byte) r.nextInt()};
        byte[] value = new byte[]{(byte) r.nextInt()};

        blockchainPersistenceUnit.put(key, value);

        byte[] readValue = blockchainPersistenceUnit.get(key);

        Assert.assertNotEquals(null, readValue);

        blockchainPersistenceUnit.destroyAndReCreate();

        readValue = blockchainPersistenceUnit.get(key);

        Assert.assertEquals(null, readValue);
        blockchainPersistenceUnit.close();
    }

    @Test
    public void testDestroy() throws IOException {


        BlockchainPersistenceUnit<String, String> blockchainPersistenceUnit =
                new BlockchainPersistenceUnit<>(BaseBlockchainTest.BLOCKCHAIN_BLOCK_DATA_TEST_PATH, String.class);

        byte[] key = new byte[]{(byte) r.nextInt()};
        byte[] value = new byte[]{(byte) r.nextInt()};

        blockchainPersistenceUnit.put(key, value);

        byte[] readValue = blockchainPersistenceUnit.get(key);

        Date dStart = new Date();

        blockchainPersistenceUnit.destroy();

        Date dInterim = new Date();

        long ms1 = dInterim.getTime() - dStart.getTime();
        System.out.printf("Destroy took: %d ms", ms1);
        System.out.println();
    }
}
