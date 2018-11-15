package network.elrond.p2p;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.p2p.model.P2PBroadcastChannel;
import network.elrond.p2p.model.P2PBroadcastChannelName;
import network.elrond.p2p.model.P2PConnection;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;

public class AppP2PManager {

    private static final Logger logger = LogManager.getLogger(AppP2PManager.class);

    private static final AppP2PManager INSTANCE = new AppP2PManager();

    public static AppP2PManager instance() {
        return INSTANCE;
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

    public <T> ArrayBlockingQueue<T> subscribeToChannel(Application application, P2PBroadcastChannelName channelName) {
        logger.traceEntry("params: {} {}", application, channelName);

        ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<>(50000, true);

        subscribeToChannel(application, channelName, (sender, request) -> {
            if (request == null) {
                logger.trace("request == null");
                return;
            }
            @SuppressWarnings("unchecked")
			T object = (T) request.getPayload();
            queue.put(object);

        });

        return logger.traceExit(queue);
    }

}
