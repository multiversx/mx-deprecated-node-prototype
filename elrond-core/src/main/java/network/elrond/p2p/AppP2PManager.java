package network.elrond.p2p;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;

public class AppP2PManager {

    private static final Logger logger = LogManager.getLogger(AppP2PManager.class);

    private static AppP2PManager instance = new AppP2PManager();

    public static AppP2PManager instance() {
        return instance;
    }


    public P2PBroadcastChanel subscribeToChannel(Application application, P2PChannelName channelName, P2PChannelListener listener) {
        logger.traceEntry("params: {} {} {}", application, channelName, listener);

        AppState state = application.getState();
        P2PConnection connection = state.getConnection();

        P2PBroadcastChanel channel = state.getChanel(channelName);
        if (channel == null) {
            logger.trace("channel NULL, creating...");
            channel = AppServiceProvider.getP2PBroadcastService().createChannel(connection, channelName);
        }
        AppServiceProvider.getP2PBroadcastService().subscribeToChannel(channel);
        state.addChanel(channel);

        channel.getListeners().add(listener);

        return logger.traceExit(channel);
    }

    public <T> ArrayBlockingQueue<T> subscribeToChannel(Application application, P2PChannelName channelName) {
        logger.traceEntry("params: {} {}", application, channelName);

        ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<>(50000, true);

        subscribeToChannel(application, channelName, (sender, request) -> {
            if (request == null) {
                logger.trace("request == null");
                return;
            }
            T object = (T) request.getPayload();
            queue.put(object);

        });

        return logger.traceExit(queue);
    }

    public <T> void subscribeTransactionPoolToTransactionChannel(Application application, P2PChannelName channelName) {
        logger.traceEntry("params: {} {}", application, channelName);

        Util.check(application != null, "application != null");
        Util.check(application.getState() != null, "state != null");
        Util.check(application.getState().getBlockchain() != null, "blockchain != null");

        subscribeToChannel(application, channelName, (sender, request) -> {
            if (request == null) {
                logger.trace("request == null");
                return;
            }
            T object = (T) request.getPayload();
            application.getState().getBlockchain().getTransactionPool().put(object.toString());

        });

        logger.traceExit();
    }

}
