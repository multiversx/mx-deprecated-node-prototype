package network.elrond.chronology;

import junit.framework.TestCase;
import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.core.ThreadUtil;
import network.elrond.processor.AppTasks;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

public class ChronologyServiceTest {
    @Test(expected = IllegalArgumentException.class)
    public void testGetRoundFromDateTimeBadConfigShouldThrowException() {
        ChronologyService chronologyService = new ChronologyServiceImpl(0);
        chronologyService.getRoundFromDateTime(0, 0);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testGetRoundFromDateTimeShouldThrowExceptionOnBadArguments(){
        ChronologyService chronologyService = new ChronologyServiceImpl(4000);
        chronologyService.getRoundFromDateTime(10000, 9999);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsDateTimeInRoundWithNullShouldThrowException() {
        ChronologyService chronologyService = new ChronologyServiceImpl();
        chronologyService.isDateTimeInRound(null, 1);
    }

    @Test
    public void testIsDateTimeInRound(){
        ChronologyService chronologyService = new ChronologyServiceImpl(4000);

        Round r = new Round();
        r.setStartTimeStamp(10000);

        TestCase.assertFalse(chronologyService.isDateTimeInRound(r, 1));
        TestCase.assertFalse(chronologyService.isDateTimeInRound(r, 9999));
        TestCase.assertTrue(chronologyService.isDateTimeInRound(r, 10000));
        TestCase.assertTrue(chronologyService.isDateTimeInRound(r, 10001));
        TestCase.assertTrue(chronologyService.isDateTimeInRound(r, 13999));
        TestCase.assertFalse(chronologyService.isDateTimeInRound(r, 14000));
    }

    @Test
    public void testGetRoundFromDateTime() {
        ChronologyService chronologyService = new ChronologyServiceImpl(4000);

        Round r = chronologyService.getRoundFromDateTime(10000, 10000);

        TestCase.assertEquals(0, r.getIndex());
        TestCase.assertEquals(10000, r.getStartTimeStamp());

        r = chronologyService.getRoundFromDateTime(10001, 10002);

        TestCase.assertEquals(0, r.getIndex());
        TestCase.assertEquals(10001, r.getStartTimeStamp());

        r = chronologyService.getRoundFromDateTime(10001, 13002);

        TestCase.assertEquals(0, r.getIndex());
        TestCase.assertEquals(10001, r.getStartTimeStamp());

        r = chronologyService.getRoundFromDateTime(10001, 14000);

        TestCase.assertEquals(0, r.getIndex());
        TestCase.assertEquals(10001, r.getStartTimeStamp());

        r = chronologyService.getRoundFromDateTime(10001, 14001);

        TestCase.assertEquals(1, r.getIndex());
        TestCase.assertEquals(14001, r.getStartTimeStamp());

        r = chronologyService.getRoundFromDateTime(10001, 18000);

        TestCase.assertEquals(1, r.getIndex());
        TestCase.assertEquals(14001, r.getStartTimeStamp());

        r = chronologyService.getRoundFromDateTime(10001, 18001);

        TestCase.assertEquals(2, r.getIndex());
        TestCase.assertEquals(18001, r.getStartTimeStamp());

        System.out.println(r);
    }

    @Test
    public void testGetNtpTime() throws Exception{
        Application application = new Application(new AppContext());

        AppTasks.NTP_CLIENT_INITIALIZER.process(application);

        ThreadUtil.sleep(1000);

        ChronologyService chronologyService = AppServiceProvider.getChronologyService();

        TestCase.assertNotNull(application.getState().getNtpClient());

        application.getState().getNtpClient().setPollMs(100);

        ThreadUtil.sleep(2000);

        long currentDateNTP = chronologyService.getSynchronizedTime(application.getState().getNtpClient());
        long currentDateLocal = System.currentTimeMillis();

        System.out.println(String.format("NTP time: %d", currentDateNTP));
        System.out.println(String.format("Local time: %d", currentDateLocal));
        System.out.println(String.format("Difference: %d ms", currentDateNTP - currentDateLocal));

        TestCase.assertFalse(application.getState().getNtpClient().isOffline());

        System.out.println("Host: " + application.getState().getNtpClient().getCurrentHostName());
    }

}
