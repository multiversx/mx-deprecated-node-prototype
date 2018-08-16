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
    BlockchainPersistenceUnit<String, String> blockchainPersistenceUnit;
    @Before
    public void setUp() throws IOException {

        r = new Random();
        blockchainPersistenceUnit = new BlockchainPersistenceUnit<>("test", String.class);
    }

    @After
    public void TearDown() throws IOException {
        blockchainPersistenceUnit.close();
        blockchainPersistenceUnit.destroy();
    }
    @Test(expected = IllegalArgumentException.class)
    public void testBlockchainPersistenceUnitConstructorWithNullDatabasePathShouldThrowException() {
        blockchainPersistenceUnit.put(null, new byte[3]);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testBlockchainPersistenceUnitWithNullPathShouldThrowException() {
        blockchainPersistenceUnit.put(null, new byte[3]);
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

        blockchainPersistenceUnit.recreate();

        readValue = blockchainPersistenceUnit.get(key);

        Assert.assertNull(readValue);
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
