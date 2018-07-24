package network.elrond.api;

import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class WriteLogTest {

    private static Logger logger = LogManager.getLogger(WriteLogTest.class);

    @Test
    public void testLog(){
        Util.changeLogsPath("logs/" + Util.getHostName() + " - TEST LOGS");

        logger.debug("test");

        long dtStart = System.currentTimeMillis();

        int percent = 0;
        int oldPercent = 0;

        for (int i = 0; i < 10; i++){
            logger.error("Write benchmark {}", i);

            percent = i / 100;

            if (percent != oldPercent){
                System.out.println(String.format("Progress: %d percent", percent));
                oldPercent = percent;
            }
        }

        long dtEnd = System.currentTimeMillis();

        logger.info("Took: {} ms", dtEnd - dtStart);

        ThreadUtil.sleep(5000);

    }

}
