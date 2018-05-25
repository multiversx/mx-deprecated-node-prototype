package network.elrond.blockchain;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class BlockchainPersistenceUnitTest {

    File currentPath;
    Random r;
    @Before
    public void setUp(){
        r = new Random();
        currentPath = new File(System.getProperty("user.dir"), r.nextInt() + "");
        currentPath.mkdirs();
    }

    @After
    public void TearDown() throws IOException, InterruptedException {
        Thread.sleep(200);
        FileUtils.deleteDirectory(currentPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockchainPersistenceUnitWithNullPathShouldThrowException() throws IOException {
        BlockchainPersistenceUnit<String, String> blockchainPersistenceUnit =
                new BlockchainPersistenceUnit<String,String>((String)null, String.class);
    }

    @Test
    public void testPutNullObjectShouldThrowException() throws IOException {
        BlockchainPersistenceUnit<String, String> blockchainPersistenceUnit =
                new BlockchainPersistenceUnit<String,String>(currentPath.getPath(), String.class);
        byte[] key = new byte[] { (byte)r.nextInt()};
        byte[] value = new byte[] { (byte)r.nextInt()};
         blockchainPersistenceUnit.put(key,value);
         byte[] readValue = blockchainPersistenceUnit.get(key);
        Assert.assertArrayEquals(value, readValue);
    }
}
