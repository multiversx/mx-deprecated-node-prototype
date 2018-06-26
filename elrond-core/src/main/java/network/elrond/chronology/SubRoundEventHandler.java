package network.elrond.chronology;

import network.elrond.Application;
import network.elrond.core.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

//public class SubRoundEventHandler implements EventHandler<SubRound, ArrayBlockingQueue<String>> {
//    private static final Logger logger = LogManager.getLogger(ChronologyServiceImpl.class);
//
//
//    public void onEvent(Application application, Object sender, SubRound data, ArrayBlockingQueue<String> queue) {
//        // just display data for now
//        StringBuilder stringBuilder = new StringBuilder();
//
//        String strSender = "[NULL SENDER]";
//        if (sender != null){
//            strSender = sender.toString();
//        }
//
//        NTPClient ntpClient = application.getState().getNtpClient();
//        String timeType = "NOT_SYNCED";
//        long currentMillis = 0;
//        if (ntpClient == null){
//            currentMillis = System.currentTimeMillis();
//        }else {
//            if (ntpClient.isOffline()){
//                currentMillis = System.currentTimeMillis();
//            } else {
//                currentMillis = ntpClient.currentTimeMillis();
//                timeType = "SYNCED";
//            }
//        }
//
//        stringBuilder.append(String.format("Event from %1$s, time %2$s: %3$tY.%3$tm.%3$td %3$tT.%4$03d: ", strSender, timeType, new Date(currentMillis), currentMillis % 1000));
//
//        if (data == null){
//            stringBuilder.append("[NULL DATA]");
//        } else {
//            stringBuilder.append(data);
//        }
//
//        logger.trace(stringBuilder.toString());
//    }
//}
