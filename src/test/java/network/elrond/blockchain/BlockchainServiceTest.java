package network.elrond.blockchain;

import network.elrond.data.Block;
import network.elrond.data.BlockService;
import network.elrond.data.DataBlock;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

public class BlockchainServiceTest {
    private Random rdm = new Random(1000);

    @Test
    public void testBlockchainService() {
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
        BlockService blockService = AppServiceProvider.getBlockService();

        BlockchainContext context = new BlockchainContext();
        context.setDatabasePath(BlockchainUnitType.BLOCK, "blockchain.block.data-test");
        context.setDatabasePath(BlockchainUnitType.TRANSACTION, "blockchain.transaction.data-test");

        context.setDatabasePath(BlockchainUnitType.SETTINGS, "blockchain.settings.data-test");

        try {
            Blockchain blkc = new Blockchain(context);

            for (int i = 0; i < 100000; i++) {
                Block blk = GenerateRandomBlock();

                if (i % 100 == 0) {
                    System.out.println("Reached " + Integer.toString(i) + " blocks...");
                }


                blockchainService.put(new String(Base64.encode(blockService.getHash(blk, true))), blk, blkc, BlockchainUnitType.BLOCK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private Block GenerateRandomBlock() {
        byte[] buff = new byte[32];

        DataBlock db = new DataBlock();
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
