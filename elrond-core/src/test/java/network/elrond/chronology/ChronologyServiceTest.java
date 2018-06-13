package network.elrond.chronology;

import junit.framework.TestCase;
import network.elrond.consensus.Validator;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Date;

public class ChronologyServiceTest {
    @Test(expected = NullPointerException.class)
    public void testIsDateTimeInEpochWithNullShouldThrowException() {
        ChronologyService chronologyService = new ChronologyServiceImpl();
        chronologyService.isDateTimeInEpoch(null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testGetRoundFromDateTimeWithNullShouldThrowException() {
        ChronologyService chronologyService = new ChronologyServiceImpl();
        chronologyService.getRoundFromDateTime(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRoundFromDateTimeWithNotInRangeShouldThrowException() {
        ChronologyService chronologyService = new ChronologyServiceImpl();

        Epoch epoch = new Epoch();
        epoch.setDateMsEpochStarts(1);

        Round round = chronologyService.getRoundFromDateTime(epoch, 500000000);
    }

    @Test(expected = NullPointerException.class)
    public void testGenerateNewEpochWithNullShouldThrowException() {
        ChronologyService chronologyService = new ChronologyServiceImpl();
        chronologyService.generateNewEpoch(null);
    }

    @Test
    public void testGetSecondsInEpoch(){
        ChronologyService chronologyService = new ChronologyServiceImpl(10, 5);

        TestCase.assertEquals(chronologyService.getMillisecondsInEpoch(), 10 * 5 * 100);
    }

    @Test
    public void testIsDateTimeInEpoch(){
        ChronologyService chronologyService = new ChronologyServiceImpl(28800, 4);

        Epoch epoch = new Epoch();
        epoch.setDateMsEpochStarts(1); //1 - 28800 * 4 * 100 + 1

        TestCase.assertTrue(chronologyService.isDateTimeInEpoch(epoch, 1));
        TestCase.assertTrue(chronologyService.isDateTimeInEpoch(epoch, 28800));
        TestCase.assertFalse(chronologyService.isDateTimeInEpoch(epoch, 0));
        TestCase.assertTrue(chronologyService.isDateTimeInEpoch(epoch, 28800*100*4));
        TestCase.assertFalse(chronologyService.isDateTimeInEpoch(epoch, 28800*100*4 + 1));
        TestCase.assertFalse(chronologyService.isDateTimeInEpoch(epoch, 28800*100*4 + 2));
    }

    @Test
    public void testGetRoundFromDateTime() {
        ChronologyService chronologyService = new ChronologyServiceImpl(28800, 4);

        Epoch epoch = new Epoch();
        epoch.setDateMsEpochStarts(1); //1 - 28800 * 4 * 100 + 1

        TestCase.assertEquals(0, chronologyService.getRoundFromDateTime(epoch, 100).getRoundHeight());
        TestCase.assertEquals(0, chronologyService.getRoundFromDateTime(epoch, 200).getRoundHeight());
        TestCase.assertEquals(0, chronologyService.getRoundFromDateTime(epoch, 400).getRoundHeight());
        TestCase.assertEquals(1, chronologyService.getRoundFromDateTime(epoch, 401).getRoundHeight());
        TestCase.assertEquals(1, chronologyService.getRoundFromDateTime(epoch, 500).getRoundHeight());
        TestCase.assertEquals(28799, chronologyService.getRoundFromDateTime(epoch, 28800 * 4 * 100).getRoundHeight());

        TestCase.assertFalse(chronologyService.getRoundFromDateTime(epoch, 1).isLastRoundInEpoch());
        TestCase.assertFalse(chronologyService.getRoundFromDateTime(epoch, 401).isLastRoundInEpoch());
        TestCase.assertTrue(chronologyService.getRoundFromDateTime(epoch, 28800 * 4 * 100).isLastRoundInEpoch());
    }

    @Test
    public void testGenerateNewEpoch(){
        ChronologyService chronologyService = new ChronologyServiceImpl(28800, 4);

        Epoch epoch = new Epoch();
        epoch.setDateMsEpochStarts(1);
        epoch.getListEligible().add(new Validator("a"));
        epoch.getListWaiting().add(new Validator("b"));

        Epoch epochNext = chronologyService.generateNewEpoch(epoch);

        TestCase.assertEquals(epoch.getDateMsEpochStarts() + (28800 * 4 * 100), epochNext.getDateMsEpochStarts());
        TestCase.assertEquals(0, epochNext.getListWaiting().size());
        TestCase.assertEquals(2, epochNext.getListEligible().size());
        TestCase.assertEquals(epoch.getEpochHeight() + 1, epochNext.getEpochHeight());
    }

    @Test
    public void testGetNtpTime() throws Exception{
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();

        TestCase.assertNotNull(chronologyService.getNtpClient());

        chronologyService.getNtpClient().setPollMs(100);

        Thread.sleep(2000);

        long currentDateNTP = chronologyService.getSynchronizedTime();
        long currentDateLocal = System.currentTimeMillis();

        System.out.println(String.format("NTP time: %d", currentDateNTP));
        System.out.println(String.format("Local time: %d", currentDateLocal));
        System.out.println(String.format("Difference: %d ms", currentDateNTP - currentDateLocal));

        TestCase.assertFalse(chronologyService.getNtpClient().isOffline());

        System.out.println("Host: " + chronologyService.getNtpClient().getCurrentHostName());
    }

}
