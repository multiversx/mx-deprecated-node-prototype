package network.elrond.consensus;

import junit.framework.TestCase;
import network.elrond.service.AppServiceProvider;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class ValidatorTest {
    ValidatorService vs = AppServiceProvider.getValidatorService();

    @Test
    public void testComputeScore() {
        ConsensusAnswerType cat = ConsensusAnswerType.NOT_AVAILABLE;

        TestCase.assertEquals(0,  vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0, 0, 0)));
        TestCase.assertEquals(1, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0,  0, 1)));
        TestCase.assertEquals(0, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0,  1, 0)));
        TestCase.assertEquals(1, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0,  1, 1)));
        TestCase.assertEquals(2, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0,  1, 2)));
        TestCase.assertEquals(6, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0,  0, 10)));
        TestCase.assertEquals(4, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0,  10, 0)));
        TestCase.assertEquals(10, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0, 10, 10)));
        TestCase.assertEquals(10, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0, 100, 100)));
        TestCase.assertEquals(0, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0,  0, -10)));
        TestCase.assertEquals(0, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0, -10, -10)));
        TestCase.assertEquals(0, vs.computeValidatorScore(new Validator("testKey", "", cat, BigInteger.ZERO, 0,  -10, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyConstructorWithNullShouldThrowException(){
        Validator v = new Validator((Validator) null);
    }

    @Test
    public void testCopyConstructorShouldCopyValules(){
        Validator v = new Validator("test","ip", ConsensusAnswerType.NOT_AVAILABLE, BigInteger.valueOf(7), 10);
        Validator copyV = new Validator(v);

        Assert.assertTrue(v.getPubKey() == copyV.getPubKey());
        Assert.assertTrue(v.getIP() == copyV.getIP());
        Assert.assertTrue(v.getAnswer() == copyV.getAnswer());
        Assert.assertTrue(v.getRating()== copyV.getRating());
        Assert.assertTrue(v.getStake() == copyV.getStake());
    }
}
