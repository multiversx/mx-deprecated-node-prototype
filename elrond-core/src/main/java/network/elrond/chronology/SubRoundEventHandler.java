package network.elrond.chronology;

import network.elrond.Application;
import network.elrond.core.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class SubRoundEventHandler implements EventHandler<SubRound> {
    private static Logger logger = LoggerFactory.getLogger(SubRoundEventHandler.class.getName());


    public void onEvent(Application application, Object sender, SubRound data) {
        //just display data for now
        StringBuilder stringBuilder = new StringBuilder();

        String strSender = "[NULL SENDER]";
        if (sender != null){
            strSender = sender.toString();
        }

        NTPClient ntpClient = application.getState().getNtpClient();
        String timeType = "NOT_SYNCED";
        long currentMillis = 0;
        if (ntpClient == null){
            currentMillis = System.currentTimeMillis();
        }else {
            if (ntpClient.isOffline()){
                currentMillis = System.currentTimeMillis();
            } else {
                currentMillis = ntpClient.currentTimeMillis();
                timeType = "SYNCED";
            }
        }

        stringBuilder.append(String.format("Event from %1$s, time %2$s: %3$tY.%3$tm.%3$td %3$tT.%4$03d: ", strSender, timeType, new Date(currentMillis), currentMillis % 1000));

        if (data == null){
            stringBuilder.append("[NULL DATA]");
        } else {
            stringBuilder.append(data);
        }

        //logger.info(stringBuilder.toString());

        System.out.println(stringBuilder.toString());
    }



}
