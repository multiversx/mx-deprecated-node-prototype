package network.elrond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadUtil {

    private static final Logger logger = LogManager.getLogger(ThreadUtil.class);

    public static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);


    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.throwing(e);
        }
    }

    public static void submit(Runnable runnable) {
        executor.submit(runnable);
    }


}
