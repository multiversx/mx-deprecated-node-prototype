package network.elrond.chronology;

import junit.framework.TestCase;
import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.core.ThreadUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

public class NTPClientTest {

    @Test (expected = IllegalArgumentException.class)
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

        NTPClient ntp = new NTPClient(Arrays.asList("", "time.google.com"), 100);

        Thread.sleep(1000);

        long time = ntp.currentTimeMillis();

        System.out.println(time);
        System.out.println(new Date(time));

        TestCase.assertFalse(ntp.isOffline());
        TestCase.assertEquals("time.google.com", ntp.getCurrentHostName());
    }

    @Test
    public void getTimeFromAppContext() throws Exception{
        Application app = new Application(new AppContext());

        NTPClient ntpClient = new NTPClient(app.getContext().getListNTPServers(), 100);

        System.out.println("Waiting...");
        ThreadUtil.sleep(500);

        System.out.println(String.format("Host: %s, offline: %b, time: %d", ntpClient.getCurrentHostName(),
                ntpClient.isOffline(), ntpClient.currentTimeMillis()));

    }

    @Test
    public void testNTPClient(){

    }
}
