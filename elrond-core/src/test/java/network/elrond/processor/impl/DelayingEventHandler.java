package network.elrond.processor.impl;

import junit.framework.TestCase;
import network.elrond.Application;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.NTPClient;
import network.elrond.chronology.RoundState;
import network.elrond.chronology.SubRound;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import network.elrond.service.AppServiceProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DelayingEventHandler implements EventHandler<SubRound> {
    private static Random rdm = null;

    public void onEvent(Object sender, SubRound data){
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();
        NTPClient ntpClient = chronologyService.getNtpClient();
        TestCase.assertNotNull(ntpClient);
        long currentTimeStamp = chronologyService.getSynchronizedTime();

        System.out.println(String.format("Time %s %b [%d] - onEvent called", getTimeStamp(currentTimeStamp),
                ntpClient.isOffline(), currentTimeStamp));

        if (rdm == null){
            rdm = new Random(System.currentTimeMillis());
        }

        TestCase.assertNotNull(sender);
        TestCase.assertNotNull(data);

        currentTimeStamp = chronologyService.getSynchronizedTime();
        System.out.println(String.format("Time %s %b [%d] - %s @ %d [round timestamp %d, delta %d]", getTimeStamp(currentTimeStamp),
                ntpClient.isOffline(), currentTimeStamp, data.getRoundState().name(), data.getTimeStamp(), data.getRound().getStartTimeStamp(),
                data.getRound().getStartTimeStamp() - data.getTimeStamp()));

        boolean isRoundTransisionStates = (data.getRoundState() == RoundState.START_ROUND) || (data.getRoundState() == RoundState.END_ROUND);
        if (isRoundTransisionStates){
            return;
        }

        boolean isRoundOK = chronologyService.isDateTimeInRound(data.getRound(), data.getTimeStamp());
        boolean isRoundStateOK = chronologyService.computeRoundState(data.getRound().getStartTimeStamp(), data.getTimeStamp()) == data.getRoundState();

        TestCase.assertTrue(isRoundOK);
        TestCase.assertTrue(isRoundStateOK);

        int millisecondsToWaitUseless = rdm.nextInt((int)chronologyService.getRoundTimeDuration()) / 2;

        currentTimeStamp = chronologyService.getSynchronizedTime();
        System.out.println(String.format("Time %s %b [%d] - Waiting %d ms...", getTimeStamp(currentTimeStamp), ntpClient.isOffline(), currentTimeStamp, millisecondsToWaitUseless));

        ThreadUtil.sleep(millisecondsToWaitUseless);

        currentTimeStamp = chronologyService.getSynchronizedTime();
        System.out.println(String.format("Time %s %b [%d] - Done waiting %d ms...", getTimeStamp(currentTimeStamp), ntpClient.isOffline(), currentTimeStamp, millisecondsToWaitUseless));
    }

    private String getTimeStamp(long timeStamp){
        return(String.format("%1$tY.%1$tm.%1$td %1$tT.%2$03d", new Date(timeStamp), timeStamp % 1000));
    }
}
