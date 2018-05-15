package network.elrond.processor;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppManager;
import network.elrond.application.AppState;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PBroadcastConnection;
import network.elrond.p2p.P2PBroadcastService;
import network.elrond.p2p.P2PBroadcastServiceImpl;
import org.slf4j.LoggerFactory;


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

        Thread threadProcessTxHashes = new Thread(() -> {
            AppState state = application.getState();

            while (state.isStillRunning()) {
                String strHash = state.popFromTxToProcess();

                if (strHash == null) {
                    continue;
                }

                if (!application.getState().containsTxHashInTxPool(strHash))
                {
                    try {
                        Object objData = P2PBroadcastServiceImpl.instance().get(application.getState().getChanel(channelName), strHash);

                        if (objData != null) {
                            application.getState().addTxToTxPool(strHash, new Transaction(objData.toString()));
                            LoggerFactory.getLogger(AppProcessors.class).info("Got tx hash: " + strHash);
                        }

                        LoggerFactory.getLogger(AppProcessors.class).info("Tx pool size: " + application.getState().getTxPoolSize());

                    } catch (Exception ex) {

                    }
                }
            }
        });
        threadProcessTxHashes.start();


        AppManager.instance().subscribeToChannel(application, channelName, (sender, request) -> {
            if (request != null)
            {
                String strPayload = request.toString();
                //test if it's a tx hash
                if (strPayload.startsWith("H:")) {
                    strPayload = strPayload.substring(2);
                    application.getState().pushToTxToProcess(strPayload);
                }
            }
           //System.err.println(sender + " - " + request);
        });

    };


}
