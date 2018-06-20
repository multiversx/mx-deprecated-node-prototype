package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.core.ThreadUtil;
import network.elrond.processor.AppTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public abstract class AbstractBlockTask implements AppTask {

    private static final Logger logger = LogManager.getLogger(AbstractBlockTask.class);

    @Override
    public void process(Application application) throws IOException {

        Thread threadProcess = new Thread(() -> {
            logger.traceEntry();

            AppState state = application.getState();

            while (state.isStillRunning()) {

                try {
                    if (state.isLock()) {
                        ThreadUtil.sleep(100);
                        continue;
                    }

                    state.setLock();
                    logger.trace("doing some work...");
                    doProcess(application);
                    state.clearLock();

                    logger.trace("waiting...");
                    ThreadUtil.sleep(5000);
                } catch (Exception ex) {
                    logger.catching(ex);
                }

            }

            logger.traceExit();
        });
        threadProcess.start();
    }

    protected abstract void doProcess(Application application);

}
