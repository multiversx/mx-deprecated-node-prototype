package network.elrond.data;

import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.service.AppServiceProvider;
import org.spongycastle.util.encoders.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockchainTest extends BaseBlockchainTest{
    private SerializationService serializationService = AppServiceProvider.getSerializationService();

    Blockchain blockchain;

    @Before
    public void setUp() throws IOException {


        if (blockchain != null) {
            return;
        }

       blockchain = new Blockchain(getDefaultTestBlockchainContext());
    }


    @Test
    public void testNullBlock() throws IOException, ClassNotFoundException {

        String hash = "kKmANYmd+WewCmBwLmK2id7ry/Zz8mExKwFZFxyTMDQ=";
        Block block = AppServiceProvider.getBlockchainService().get(hash, blockchain, BlockchainUnitType.BLOCK, false);

        Assert.assertNull(block);
    }

    //@Test
    public void testSimpleBlock() throws IOException, ClassNotFoundException {


        Random rdm = new Random();
        Map<String, Block> blocks = new HashMap<>();

        for (int i = 0; i < 100000; i++) {

            Block block = new Block();
            BigInteger nonce = BigInteger.ONE.add(BigInteger.valueOf(i));
            block.setNonce(nonce);
            for (int d = 0; d < 5; d++) {
                byte[] buff = new byte[32];
                rdm.nextBytes(buff);
                BlockUtil.addTransactionInBlock(block, buff);
            }

            String hash = new String(Base64.encode(serializationService.getHash(block)
            ));
            blocks.put(hash, block);

            AppServiceProvider.getBlockchainService().put(hash, block, blockchain, BlockchainUnitType.BLOCK);

        }

        // Flush memory and read from database engine
        blockchain.flush();


        for (String hash : blocks.keySet()) {

            Block _block = blocks.get(hash);
            Block block = AppServiceProvider.getBlockchainService().get(hash, blockchain, BlockchainUnitType.BLOCK, false);
            Assert.assertEquals(block.nonce, _block.nonce);
            String b1Js = AppServiceProvider.getSerializationService().encodeJSON(_block);
            String b2Js = AppServiceProvider.getSerializationService().encodeJSON(block);
            Assert.assertEquals(b1Js, b2Js);

            System.out.println(b1Js);
        }

        //1484


    }
}
