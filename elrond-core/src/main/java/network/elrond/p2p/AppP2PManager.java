package network.elrond.p2p;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.service.AppServiceProvider;

import java.util.concurrent.ArrayBlockingQueue;

public class AppP2PManager {

    private static AppP2PManager instance = new AppP2PManager();

    public static AppP2PManager instance() {
        return instance;
    }


    public P2PBroadcastChanel subscribeToChannel(Application application, P2PChannelName channelName, P2PChannelListener listener) {

        AppState state = application.getState();
        P2PConnection connection = state.getConnection();

        P2PBroadcastChanel channel = state.getChanel(channelName);
        if (channel == null) {
            channel = AppServiceProvider.getP2PBroadcastService().createChannel(connection, channelName);
        }
        AppServiceProvider.getP2PBroadcastService().subscribeToChannel(channel);
        state.addChanel(channel);

        channel.getListeners().add(listener);

        return channel;

    }

    public <T> ArrayBlockingQueue<T> subscribeToChannel(Application application, P2PChannelName channelName) {

        ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<>(50000, true);

        subscribeToChannel(application, channelName, (sender, request) -> {
            if (request == null) {
                return;
            }
            T object = (T) request.getPayload();
            queue.put(object);

        });
        return queue;
    }


}
