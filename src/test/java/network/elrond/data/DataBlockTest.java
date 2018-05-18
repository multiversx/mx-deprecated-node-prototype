package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.data.Block;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.math.BigInteger;

public class DataBlockTest {
    BlockService blks = AppServiceProvider.getBlockService();

    @Test
    public void testBlock() {
        DataBlock db = new DataBlock();
        db.nonce = BigInteger.ONE;
        db.shard = 0;
        db.appStateHash = new byte[]{0, 45, 22, -10, 23, -123};

        db.listTXHashes.add( new byte[]{1, 2, 3, 4, 5});
        db.listTXHashes.add( new byte[]{6, 7, 8, 9, 10});
        db.listTXHashes.add( new byte[]{11, 12, 13, 14, 15});

        db.prevBlockHash = new byte[]{-128, -127, -126, -125, -124};

        db.listPubKeys.add("025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae");
        db.listPubKeys.add("025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4af");

        System.out.println(blks.encodeJSON(db, true));

        Block db2 = blks.decodeJSON(blks.encodeJSON(db, true));
        System.out.println(blks.encodeJSON(db2, true));

        TestCase.assertEquals(blks.encodeJSON(db, true), blks.encodeJSON(db2, true));
    }

    @Test
    public void testBlock2() {
        DataBlock db = new DataBlock();
        db.nonce = BigInteger.ONE;
        db.shard = 0;
        db.appStateHash = new byte[]{0, 45, 22, -10, 23, -123};

        db.prevBlockHash = new byte[]{-128, -127, -126, -125, -124};

        System.out.println(blks.encodeJSON(db, true));

        Block db2 = blks.decodeJSON(blks.encodeJSON(db, true));
        System.out.println(blks.encodeJSON(db2, true));

        TestCase.assertEquals(blks.encodeJSON(db, true), blks.encodeJSON(db2, true));
    }
}
