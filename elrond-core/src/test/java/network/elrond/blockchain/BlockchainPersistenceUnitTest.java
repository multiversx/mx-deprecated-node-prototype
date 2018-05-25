package network.elrond.blockchain;

import network.elrond.data.BaseBlockchainTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class BlockchainPersistenceUnitTest {
    Random r;

    @Before
    public void setUp(){
        r = new Random();
    }

    @After
    public void TearDown() throws IOException, InterruptedException {
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockchainPersistenceUnitWithNullPathShouldThrowException() throws IOException {
        BlockchainPersistenceUnit<String, String> blockchainPersistenceUnit =
                new BlockchainPersistenceUnit<String,String>((String)null, String.class);
    }

    @Test
    public void testPutNullObjectShouldThrowException() throws IOException {
        BlockchainPersistenceUnit<String, String> blockchainPersistenceUnit =
                new BlockchainPersistenceUnit<String,String>(BaseBlockchainTest.BLOCKCHAIN_BLOCK_DATA_TEST_PATH, String.class);
        byte[] key = new byte[] { (byte)r.nextInt()};
        byte[] value = new byte[] { (byte)r.nextInt()};
         blockchainPersistenceUnit.put(key,value);
         byte[] readValue = blockchainPersistenceUnit.get(key);
        Assert.assertArrayEquals(value, readValue);
    }
}
