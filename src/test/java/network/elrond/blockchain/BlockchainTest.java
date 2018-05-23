package network.elrond.blockchain;

import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Block;
import network.elrond.data.DataBlock;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockchainTest {


    static Blockchain blockchain;

    @Before
    public void setUp() throws IOException {


        if (blockchain != null) {
            return;
        }

        BlockchainContext context = new BlockchainContext();
        context.setDatabasePath(BlockchainUnitType.BLOCK, "blockchain.block.data-test");
        context.setDatabasePath(BlockchainUnitType.TRANSACTION, "blockchain.transaction.data-test");

        blockchain = new Blockchain(context);
    }


    @Test
    public void testNullBlock() throws IOException, ClassNotFoundException {

        String hash = "kKmANYmd+WewCmBwLmK2id7ry/Zz8mExKwFZFxyTMDQ=";
        Block block = AppServiceProvider.getBlockchainService().get(hash, blockchain, BlockchainUnitType.BLOCK);

        Assert.assertNull(block);
    }

    @Test
    public void testSimpleBlock() throws IOException, ClassNotFoundException {


        Random rdm = new Random();
        Map<String, Block> blocks = new HashMap<>();

        for (int i = 0; i < 100000; i++) {

            Block block = new DataBlock();
            BigInteger nonce = BigInteger.ONE.add(BigInteger.valueOf(i));
            block.setNonce(nonce);
            for (int d = 0; d < 5; d++) {
                byte[] buff = new byte[32];
                rdm.nextBytes(buff);
                block.getListTXHashes().add(buff);
            }

            String hash = new String(Base64.encode(
                    AppServiceProvider.getBlockService().getHash(block, true)
            ));
            blocks.put(hash, block);

            AppServiceProvider.getBlockchainService().put(hash, block, blockchain, BlockchainUnitType.BLOCK);

        }

        // Flush memory and read from database engine
        blockchain.flush();


        for (String hash : blocks.keySet()) {

            Block _block = blocks.get(hash);
            Block block = AppServiceProvider.getBlockchainService().get(hash, blockchain, BlockchainUnitType.BLOCK);
            Assert.assertEquals(block.getNonce(), _block.getNonce());
            String b1Js = AppServiceProvider.getBlockService().encodeJSON(_block, true);
            String b2Js = AppServiceProvider.getBlockService().encodeJSON(block, true);
            Assert.assertEquals(b1Js, b2Js);

            System.out.println(b1Js);
        }

        //1484


    }
}