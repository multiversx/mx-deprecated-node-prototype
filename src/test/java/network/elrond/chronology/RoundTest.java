package network.elrond.chronology;

import junit.framework.TestCase;
import network.elrond.consensus.ConsensusAnswerType;
import network.elrond.consensus.Validator;
import network.elrond.core.Util;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

public class RoundTest {

    @Test
    public void testRoundCreation() {
        Epoch e = new Epoch();

        //test round r
        e.createRound();
        e.createRound();
        Round r = e.createRound();
        TestCase.assertEquals(BigInteger.valueOf(2), r.roundHeight());
    }
}
