package network.elrond.p2p;

import network.elrond.Application;
import network.elrond.application.AppState;
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


    public P2PBroadcastChannel subscribeToChannel(Application application, P2PBroadcastChannelName channelName, P2PChannelListener listener) {
        logger.traceEntry("params: {} {} {}", application, channelName, listener);

        AppState state = application.getState();
        P2PConnection connection = state.getConnection();

        P2PBroadcastChannel channel = state.getChannel(channelName);
        if (channel == null) {
            logger.trace("channel NULL, creating...");
            channel = AppServiceProvider.getP2PBroadcastService().createChannel(connection, channelName);
        }
        state.addChannel(channel);

        channel.getListeners().add(listener);

        return logger.traceExit(channel);
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayBlockingQueue<T> subscribeToChannel(Application application, P2PBroadcastChannelName channelName) {
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

}
