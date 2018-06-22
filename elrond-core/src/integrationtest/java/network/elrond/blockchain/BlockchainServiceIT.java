package network.elrond.blockchain;

import network.elrond.data.BaseBlockchainTest;
import network.elrond.data.Block;
import network.elrond.data.SerializationService;
import network.elrond.service.AppServiceProvider;
import org.spongycastle.util.encoders.Base64;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

public class BlockchainServiceIT extends BaseBlockchainTest {
    private Random rdm = new Random(1000);

    @Test
    public void testBlockchainService() throws IOException {
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
        SerializationService serializationService = AppServiceProvider.getSerializationService();

        BlockchainContext context = getDefaultTestBlockchainContext();

        try {
            Blockchain blkc = new Blockchain(context);

            for (int i = 0; i < 200; i++) {
                Block blk = GenerateRandomBlock();

                if (i % 100 == 0) {
                    System.out.println("Reached " + Integer.toString(i) + " blocks...");
                }

                blockchainService.put(new String(Base64.encode(serializationService.getHash(blk))), blk, blkc, BlockchainUnitType.BLOCK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private Block GenerateRandomBlock() {
        byte[] buff = new byte[32];

        Block db = new Block();
        db.setNonce(BigInteger.ONE);
        db.setShard(0);

        rdm.nextBytes(buff);
        db.setAppStateHash(buff);
        for (int i = 0; i < 1000; i++) {
            rdm.nextBytes(buff);
            db.getListTXHashes().add(buff);
        }

        db.getListPublicKeys().add("025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae");
        db.getListPublicKeys().add("025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4af");

        return (db);

    }
}
