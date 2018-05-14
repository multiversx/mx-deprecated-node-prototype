package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.data.Block;
import org.junit.Test;

import java.math.BigInteger;

public class DataBlockTest {
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

        System.out.println(db.encodeJSON());

        DataBlock db2 = new DataBlock();
        db2.decodeJSON(db.encodeJSON());
        System.out.println(db2.encodeJSON());

        TestCase.assertEquals(db.encodeJSON(), db2.encodeJSON());




//        Block b1 = new Block("Hello world from Elrond", "0");
////
////        String data = b1.getData();
////        TestCase.assertEquals("Hello world from Elrond", b1.getData());
////
////        String hash = b1.getHash();
////        TestCase.assertEquals("?", b1.getHash());






    }
}
