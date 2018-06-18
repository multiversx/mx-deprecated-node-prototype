package network.elrond.chronology;

import network.elrond.Application;
import network.elrond.core.EventHandler;

import java.util.Date;

public class SubRoundEventHandler implements EventHandler<SubRound> {

    public void onEvent(Application application, Object sender, SubRound data) {
        //just display data for now
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

        System.out.println(stringBuilder.toString());
    }



}
