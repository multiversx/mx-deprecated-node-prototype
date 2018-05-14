package network.elrond.consensus;

import junit.framework.TestCase;
import org.junit.Test;

import java.math.BigInteger;

public class ValidatorTest {
    @Test
    public void testComputeScore() {
        ConsensusAnswerType cat = ConsensusAnswerType.NOT_AVAILABLE;

        TestCase.assertEquals(0, new Validator("", "", cat, BigInteger.ZERO, 0, 0, 0).getScore());
        TestCase.assertEquals(1, new Validator("", "", cat, BigInteger.ZERO, 0,  0, 1).getScore());
        TestCase.assertEquals(0, new Validator("", "", cat, BigInteger.ZERO, 0,  1, 0).getScore());
        TestCase.assertEquals(1, new Validator("", "", cat, BigInteger.ZERO, 0,  1, 1).getScore());
        TestCase.assertEquals(2, new Validator("", "", cat, BigInteger.ZERO, 0,  1, 2).getScore());
        TestCase.assertEquals(6,new Validator("", "", cat, BigInteger.ZERO, 0,  0, 10).getScore());
        TestCase.assertEquals(4, new Validator("", "", cat, BigInteger.ZERO, 0,  10, 0).getScore());
        TestCase.assertEquals(10, new Validator("", "", cat, BigInteger.ZERO, 0, 10, 10).getScore());
        TestCase.assertEquals(10, new Validator("", "", cat, BigInteger.ZERO, 0, 100, 100).getScore());
        TestCase.assertEquals(0, new Validator("", "", cat, BigInteger.ZERO, 0,  0, -10).getScore());
        TestCase.assertEquals(0, new Validator("", "", cat, BigInteger.ZERO, 0, -10, -10).getScore());
        TestCase.assertEquals(0, new Validator("", "", cat, BigInteger.ZERO, 0,  -10, 0).getScore());
    }


}
