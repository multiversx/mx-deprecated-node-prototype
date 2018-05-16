package network.elrond.processor;

import network.elrond.processor.impl.P2PBlocksInterceptorProcessor;
import network.elrond.processor.impl.P2PConnectionStarterProcessor;
import network.elrond.processor.impl.P2PTransactionsInterceptorProcessor;


public class AppProcessors {


    /**
     * Init application P2P connections
     */
    public static AppProcessor P2P_CONNECTION_STARTER = (application) -> {
        new P2PConnectionStarterProcessor().process(application);
    };

    /**
     * P2P transactions broadcast
     */
    public static AppProcessor P2P_TRANSACTIONS_INTERCEPTOR = (application) -> {

        new P2PTransactionsInterceptorProcessor().process(application);
    };

    /**
     * P2P block broadcast
     */
    public static AppProcessor P2P_BLOCKS_INTERCEPTOR = (application) -> {

        new P2PBlocksInterceptorProcessor().process(application);

    };


}
