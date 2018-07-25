package network.elrond.api;

import network.elrond.api.log.MySQLAppender;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.Arrays;

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

        try{
            throw new Exception("manual exception");
        } catch (Exception ex){
            logger.throwing(ex);
            logger.fatal("Fatal stack: {}", Arrays.toString(ex.getStackTrace()));
        }


        ThreadUtil.sleep(5000);

    }

    @Test
    public void testIPAddr(){
        logger.info("IP: {}", MySQLAppender.getNodeName());
    }

}
