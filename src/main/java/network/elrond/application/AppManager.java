package network.elrond.application;

import network.elrond.Application;
import network.elrond.p2p.*;

public class AppManager {

    private static AppManager instance = new AppManager();

    public static AppManager instance() {
        return instance;
    }


    public P2PBroadcastChanel subscribeToChannel(Application application, String channelName, P2PChannelListener listener) {


        AppState state = application.getState();
        P2PBroadcastConnection connection = state.getConnection();

        P2PBroadcastChanel channel = state.getChanel(channelName);
        if (channel == null) {
            channel = P2PBroadcastServiceImpl.instance().createChannel(connection, channelName);
        }
        P2PBroadcastServiceImpl.instance().subscribeToChannel(channel);
        state.addChanel(channelName, channel);
        channel.getListeners().add(listener);

        return channel;

    }


}
