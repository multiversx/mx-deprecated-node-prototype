package network.elrond.application;

import network.elrond.Application;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelListener;
import network.elrond.p2p.P2PConnection;
import network.elrond.service.AppServiceProvider;

public class AppManager {

    private static AppManager instance = new AppManager();

    public static AppManager instance() {
        return instance;
    }


    public P2PBroadcastChanel subscribeToChannel(Application application, String channelName, P2PChannelListener listener) {


        AppState state = application.getState();
        P2PConnection connection = state.getConnection();

        P2PBroadcastChanel channel = state.getChanel(channelName);
        if (channel == null) {
            channel = AppServiceProvider.getP2PBroadcastService().createChannel(connection, channelName);
        }
        AppServiceProvider.getP2PBroadcastService().subscribeToChannel(channel);
        state.addChanel(channelName, channel);

        channel.getListeners().add(listener);

        return channel;

    }


}
