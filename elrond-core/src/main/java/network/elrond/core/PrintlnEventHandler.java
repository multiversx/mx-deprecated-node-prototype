package network.elrond.core;

import network.elrond.Application;
import network.elrond.chronology.NTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class PrintlnEventHandler implements EventHandler<String> {
    private static final Logger logger = LogManager.getLogger(PrintlnEventHandler.class);

    public void onEvent(Object sender, String data){
        StringBuilder stringBuilder = new StringBuilder();

        String strSender = "[NULL SENDER]";
        if (sender != null){
            strSender = sender.toString();
        }

        long currentMillis = System.currentTimeMillis();

        stringBuilder.append(String.format("Event from %1$s, time: %2$tY.%2$tm.%2$td %2$tT.%3$03d: ", strSender, new Date(currentMillis), currentMillis % 1000));

        if (data == null){
            stringBuilder.append("[NULL DATA]");
        } else {
            stringBuilder.append(data);
        }

        logger.trace(stringBuilder.toString());
    }
}
