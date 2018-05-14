package network.elrond.processor;

import network.elrond.application.AppContext;
import network.elrond.application.AppManager;
import network.elrond.application.AppState;
import network.elrond.p2p.P2PBroadcastConnection;
import network.elrond.p2p.P2PBroadcastServiceImpl;


public class AppProcessors {


    /**
     * Init application P2P connections
     */
    public static AppProcessor P2P_CONNECTION_STARTER = (application) -> {

        AppContext context = application.getContext();
        AppState state = application.getState();

        P2PBroadcastConnection connection = P2PBroadcastServiceImpl.instance().createConnection(context);
        state.setConnection(connection);

    };


    /**
     * P2P transactions broadcast
     */
    public static AppProcessor P2P_TRANSACTIONS_INTERCEPTOR = (application) -> {


        String channelName = "TRANSACTIONS";
        AppManager.instance().subscribeToChannel(application, channelName, (sender, request) -> {
            System.err.println(sender + " - " + request);
        });

    };


}
