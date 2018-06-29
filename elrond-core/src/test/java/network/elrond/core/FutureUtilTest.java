package network.elrond.core;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class FutureUtilTest {

    @Test
    public void testFutureUtilNoTimeout() throws Exception{
        long startMillis = System.currentTimeMillis();

        int value = FutureUtil.get(() ->{
            ThreadUtil.sleep(1000);
            return 1;
        });

        long endMillis = System.currentTimeMillis();

        System.out.println(String.format("Value: %d, took %d ms", value, endMillis-startMillis));

        TestCase.assertTrue(endMillis-startMillis >= 1000);
        TestCase.assertEquals(1, value);
    }

    @Test (expected = TimeoutException.class)
    public void testFutureUtilWithTimeout() throws Exception{

        long startMillis = System.currentTimeMillis();

        int value = FutureUtil.get(() ->{
            ThreadUtil.sleep(6000);
            return 1;
        }, 1);

        long endMillis = System.currentTimeMillis();

        System.out.println(String.format("Value: %d, took %d ms", value, endMillis-startMillis));
        Assert.fail();
    }

}
