package network.elrond.chronology;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class EpochTest {
    @Test(expected = IllegalArgumentException.class)
    public void testEpochWithIncorrectHeightShouldThrowException() {
       Epoch epoch = new Epoch();
       epoch.setEpochHeight(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEpochWithIncorrectStartDateMsShouldThrowException() {
        Epoch epoch = new Epoch();
        epoch.setDateMsEpochStarts(-1);
    }


}
