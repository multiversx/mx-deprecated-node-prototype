package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.data.model.Block;
import network.elrond.data.service.SerializationService;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.junit.Test;

import java.math.BigInteger;

public class DataBlockTest {
    @Test
    public void testBlock() {
        Block db = new Block();
        db.setNonce(BigInteger.ONE);
        db.setShard(new Shard(0));
        db.setAppStateHash(new byte[]{0, 45, 22, -10, 23, -123});

        db.getListTXHashes().add(new byte[]{1, 2, 3, 4, 5});
        db.getListTXHashes().add(new byte[]{6, 7, 8, 9, 10});
        db.getListTXHashes().add(new byte[]{11, 12, 13, 14, 15});

        db.setPrevBlockHash(new byte[]{-128, -127, -126, -125, -124});

        db.getListPublicKeys().add("025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae");
        db.getListPublicKeys().add("025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4af");

        SerializationService serializationService = AppServiceProvider.getSerializationService();
        System.out.println(serializationService.encodeJSON(db));

        Block db2 = serializationService.decodeJSON(serializationService.encodeJSON(db), Block.class);
        System.out.println(serializationService.encodeJSON(db2));

        TestCase.assertEquals(serializationService.encodeJSON(db), serializationService.encodeJSON(db2));

    }
}
