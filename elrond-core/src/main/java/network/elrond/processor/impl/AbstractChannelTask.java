package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.core.ThreadUtil;
import network.elrond.p2p.AppP2PManager;
import network.elrond.p2p.P2PChannelName;
import network.elrond.processor.AppTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class AbstractChannelTask<T> implements AppTask {
    private static final Logger logger = LogManager.getLogger(AbstractChannelTask.class);

    @Override
    public void process(Application application) {
        logger.traceEntry("params: {}", application);
        ArrayBlockingQueue<T> queue = AppP2PManager.instance().subscribeToChannel(application, getChannelName());

        Thread thread = new Thread(() -> {
            logger.traceEntry();
            AppState state = application.getState();
            while (state.isStillRunning()) {
                try {
                    logger.trace("processing...");
                    process(queue, application);
                    logger.trace("waiting...");
                    ThreadUtil.sleep(500);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            logger.traceExit();
        });
        thread.setName(getChannelName() + "_" + getClass().getName());
        thread.start();

    }

    protected void process(ArrayBlockingQueue<T> queue, Application application) {
        logger.traceEntry();
        T object = queue.poll();
        if (object == null) {
            logger.traceExit("null object!", null);
            return;
        }
        process(object, application);
    }

    protected abstract void process(T object, Application application);

    protected abstract P2PChannelName getChannelName();


}
