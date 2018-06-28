package network.elrond.consensus;

import junit.framework.TestCase;
import network.elrond.chronology.Round;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsensusServiceImplTest {
    @Test
    public void testComputeLeader(){
        List<String> listNodes = new ArrayList<>();

        listNodes.add("000");
        listNodes.add("040");
        listNodes.add("001");
        listNodes.add("005");
        listNodes.add("100");
        listNodes.add("002");
        listNodes.add("060");

        Collections.sort(listNodes);

        //test sorting is ok
        String prevValue = listNodes.get(0);
        for (int i = 1; i < listNodes.size(); i++){
            TestCase.assertTrue(prevValue.compareTo(listNodes.get(i)) < 0);

            prevValue = listNodes.get(i);
        }

        System.out.println(String.format("%s", listNodes));

        ConsensusService consensusService = new ConsensusServiceImpl();
        TestCase.assertEquals(listNodes.get(0), consensusService.computeLeader(listNodes, new Round(0, 0)));
        TestCase.assertEquals(listNodes.get(1), consensusService.computeLeader(listNodes, new Round(1, 0)));
        TestCase.assertEquals(listNodes.get(2), consensusService.computeLeader(listNodes, new Round(2, 0)));
        TestCase.assertEquals(listNodes.get(3), consensusService.computeLeader(listNodes, new Round(3, 0)));
        TestCase.assertEquals(listNodes.get(4), consensusService.computeLeader(listNodes, new Round(4, 0)));
        TestCase.assertEquals(listNodes.get(5), consensusService.computeLeader(listNodes, new Round(5, 0)));
        TestCase.assertEquals(listNodes.get(6), consensusService.computeLeader(listNodes, new Round(6, 0)));
        TestCase.assertEquals(listNodes.get(0), consensusService.computeLeader(listNodes, new Round(7, 0)));
        TestCase.assertEquals(listNodes.get(1), consensusService.computeLeader(listNodes, new Round(8, 0)));
        TestCase.assertEquals(listNodes.get(2), consensusService.computeLeader(listNodes, new Round(9, 0)));
        TestCase.assertEquals(listNodes.get(3), consensusService.computeLeader(listNodes, new Round(10, 0)));
        TestCase.assertEquals(listNodes.get(4), consensusService.computeLeader(listNodes, new Round(11, 0)));



    }
}
