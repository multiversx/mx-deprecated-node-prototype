package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.p2p.AppP2PManager;
import network.elrond.p2p.P2PChannelName;
import network.elrond.processor.AppTask;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class AbstractChannelTask<T> implements AppTask {

    @Override
    public void process(Application application) {


        ArrayBlockingQueue<T> queue = AppP2PManager.instance().subscribeToChannel(application, getChannelName());

        Thread thread = new Thread(() -> {
            AppState state = application.getState();
            while (state.isStillRunning()) {
                process(queue, application);
            }
        });
        thread.setName(getChannelName() + "_" + getClass().getName());
        thread.start();

    }

    protected void process(ArrayBlockingQueue<T> queue, Application application) {
        T object = queue.poll();
        if (object == null) {
            return;
        }
        process(object, application);
    }

    protected abstract void process(T object, Application application);

    protected abstract P2PChannelName getChannelName();


}
