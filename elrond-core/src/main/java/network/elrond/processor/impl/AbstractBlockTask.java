package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.core.ThreadUtil;
import network.elrond.processor.AppTask;

import java.io.IOException;

public abstract class AbstractBlockTask implements AppTask {

    @Override
    public void process(Application application) throws IOException {

        Thread threadProcess = new Thread(() -> {

            AppState state = application.getState();

            while (state.isStillRunning()) {

                try {
                    if (state.isLock()) {
                        ThreadUtil.sleep(100);
                        continue;
                    }

                    state.setLock();
                    doProcess(application);
                    state.clearLock();

                    ThreadUtil.sleep(5000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
        threadProcess.start();
    }

    protected abstract void doProcess(Application application);

}
