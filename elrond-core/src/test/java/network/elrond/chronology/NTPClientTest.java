package network.elrond.chronology;

import junit.framework.TestCase;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.util.Arrays;

public class NTPClientTest {

    @Test (expected = NullPointerException.class)
    public void ntpClientShouldResultinExceptionWhenListNull() throws Exception{
        NTPClient ntp = new NTPClient(null, 100);
    }

    @Test
    public void getTimeNoServerShouldResultInOfflineNtpClient() throws Exception{
        //ChronologyService chronologyService = AppServiceProvider.getChronologyService();

        NTPClient ntp = new NTPClient(Arrays.asList(""), 100);

        Thread.sleep(1000);

        ntp.currentTimeMillis();

        TestCase.assertTrue(ntp.isOffline());
    }

    @Test
    public void getTimeWithValidServersListShouldProduceValue() throws Exception{
        //ChronologyService chronologyService = AppServiceProvider.getChronologyService();

        NTPClient ntp = new NTPClient(Arrays.asList("", "time.windows.com"), 100);

        Thread.sleep(1000);

        long time = ntp.currentTimeMillis();

        System.out.println(time);

        TestCase.assertFalse(ntp.isOffline());
        TestCase.assertEquals("time.windows.com", ntp.getCurrentHostName());
    }
}
