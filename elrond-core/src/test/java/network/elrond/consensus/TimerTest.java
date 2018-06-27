package network.elrond.consensus;

import network.elrond.core.ThreadUtil;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTest {

    @Test
    public void testTimer(){
        Timer timer = new Timer("testTimer");


        TimerTask timerTask10000 = new TimerTask() {
            @Override
            public void run() {
                long lStartRun = System.currentTimeMillis();

                while(true) {
                    ThreadUtil.sleep(100);
                    long lEndRun = System.currentTimeMillis();

                    System.out.println(String.format("Running for %d ms", lEndRun - lStartRun));
                }
            }
        };

        timer.schedule(timerTask10000, 0, 1000);


        for (int i = 0; i < 5; i++){
            ThreadUtil.sleep(1000);
        }

    }

}
