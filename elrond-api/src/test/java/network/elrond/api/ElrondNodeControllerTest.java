package network.elrond.api;

import junit.framework.TestCase;
import org.junit.Test;

public class ElrondNodeControllerTest {

    @Test
    public void testShardAllocation(){
        ElrondNodeController elrondNodeController = new ElrondNodeController();

        TestCase.assertEquals((int)0, (int)elrondNodeController.ShardOfAddress(null, null));
        TestCase.assertEquals((int)0, (int)elrondNodeController.ShardOfAddress(null, ""));
        TestCase.assertEquals((int)0, (int)elrondNodeController.ShardOfAddress(null, "0302fa311fac6aa56c1a5b08e6c9bcea32fc1939cbef5010c2ab853afb5563976c"));
        TestCase.assertEquals((int)1, (int)elrondNodeController.ShardOfAddress(null, "0302fa311fac6aa56c1a5b08e6c9bcea32fc1939cbef5010c2ab853afb5563976d"));

        System.out.println("Shard: " + elrondNodeController.ShardOfAddress(null, "0302fa311fac6aa56c1a5b08e6c9bcea32fc1939cbef5010c2ab853afb5563976c"));



    }

}
