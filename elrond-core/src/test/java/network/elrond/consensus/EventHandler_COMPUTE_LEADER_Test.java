package network.elrond.consensus;

import junit.framework.TestCase;
import net.tomp2p.peers.Number160;
import network.elrond.consensus.EventHandler_COMPUTE_LEADER;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventHandler_COMPUTE_LEADER_Test {

    @Test
    public void testIfLeaderIsComputedFromAstaticList(){
        List<Number160> listNodes = new ArrayList<>();

        listNodes.add(Number160.createHash(0));
        listNodes.add(Number160.createHash(40));
        listNodes.add(Number160.createHash(1));
        listNodes.add(Number160.createHash(5));
        listNodes.add(Number160.createHash(100));
        listNodes.add(Number160.createHash(2));
        listNodes.add(Number160.createHash(60));

        Collections.sort(listNodes);

        //test sorting is ok
        Number160 prevValue = listNodes.get(0);
        for (int i = 1; i < listNodes.size(); i++){
            TestCase.assertTrue(prevValue.compareTo(listNodes.get(i)) < 0);

            prevValue = listNodes.get(i);
        }

        System.out.println(String.format("%s", listNodes));

        EventHandler_COMPUTE_LEADER compute_leader = new EventHandler_COMPUTE_LEADER();
        TestCase.assertEquals(listNodes.get(0), compute_leader.computeLeader(listNodes, 0));
        TestCase.assertEquals(listNodes.get(1), compute_leader.computeLeader(listNodes, 1));
        TestCase.assertEquals(listNodes.get(2), compute_leader.computeLeader(listNodes, 2));
        TestCase.assertEquals(listNodes.get(3), compute_leader.computeLeader(listNodes, 3));
        TestCase.assertEquals(listNodes.get(4), compute_leader.computeLeader(listNodes, 4));
        TestCase.assertEquals(listNodes.get(5), compute_leader.computeLeader(listNodes, 5));
        TestCase.assertEquals(listNodes.get(6), compute_leader.computeLeader(listNodes, 6));
        TestCase.assertEquals(listNodes.get(0), compute_leader.computeLeader(listNodes, 7));
        TestCase.assertEquals(listNodes.get(1), compute_leader.computeLeader(listNodes, 8));
        TestCase.assertEquals(listNodes.get(2), compute_leader.computeLeader(listNodes, 9));
        TestCase.assertEquals(listNodes.get(3), compute_leader.computeLeader(listNodes, 10));
        TestCase.assertEquals(listNodes.get(4), compute_leader.computeLeader(listNodes, 11));


    }

}
