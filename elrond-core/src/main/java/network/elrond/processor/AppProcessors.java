package network.elrond.processor;

import network.elrond.account.AccountState;
import network.elrond.processor.impl.*;


public class AppProcessors {


    /**
     * Init application P2P connections
     */
    public static AppProcessor BOOTSTRAP_P2P_CONNECTION = (application) -> {
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

    /**
     * Init public and private keys
     */
    public static AppProcessor INITIALIZE_PUBLIC_PRIVATE_KEYS = (application) -> {
        new AccountInitializerProcessor().process(application);
    };

    /**
     * Init blockchain
     */
    public static AppProcessor BOOTSTRAP_BLOCKCHAIN = (application) -> {
        new BlockchainStarterProcessor().process(application);
    };

    /**
     * Init blockchain
     */
    public static AppProcessor BOOTSTRAP_ACCOUNTS= (application) -> {
        new AccountsStarterProcessor().process(application);
    };


    public static AppProcessor BOOTSTRAP_SYSTEM = (application) -> {
        new BootstrappingProcessor().process(application);
    };

}
