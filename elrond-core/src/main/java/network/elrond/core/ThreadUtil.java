package network.elrond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadUtil {

    private static final Logger logger = LogManager.getLogger(ThreadUtil.class);

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.throwing(e);
        }
    }
}
