package network.elrond.api;

import junit.framework.TestCase;
import org.junit.Test;

public class ElrondNodeControllerTest {

    @Test
    public void testShardAllocation(){
        ElrondNodeController elrondNodeController = new ElrondNodeController();

        TestCase.assertFalse(elrondNodeController.shardOfAddress(null, null).isSuccess());
        TestCase.assertFalse(elrondNodeController.shardOfAddress(null, "").isSuccess());
        TestCase.assertEquals(0, elrondNodeController.shardOfAddress(null, "0302fa311fac6aa56c1a5b08e6c9bcea32fc1939cbef5010c2ab853afb5563976c").getPayload());
        TestCase.assertEquals(1, elrondNodeController.shardOfAddress(null, "0302fa311fac6aa56c1a5b08e6c9bcea32fc1939cbef5010c2ab853afb5563976d").getPayload());

        System.out.println("Shard: " + elrondNodeController.shardOfAddress(null, "0302fa311fac6aa56c1a5b08e6c9bcea32fc1939cbef5010c2ab853afb5563976c"));



    }

}
