package network.elrond.processor;

import network.elrond.processor.impl.executor.BootstrapBlockTask;
import network.elrond.processor.impl.executor.ChronologyBlockTask;
import network.elrond.processor.impl.executor.SynchronizationBlockTask;
import network.elrond.processor.impl.initialization.*;
import network.elrond.processor.impl.interceptor.P2PBlocksInterceptorProcessor;
import network.elrond.processor.impl.interceptor.P2PReceiptInterceptorProcessor;
import network.elrond.processor.impl.interceptor.P2PTransactionsInterceptorProcessor;
import network.elrond.processor.impl.interceptor.P2PXTransactionsInterceptorProcessor;


public class AppTasks {


    /**
     * Init application P2P connections
     */
    public static AppTask INIT_P2P_CONNECTION = (application) -> {
        new P2PConnectionStarterProcessor().process(application);
    };

    /**
     * P2P transactions broadcast
     */
    public static AppTask INTERCEPT_TRANSACTIONS = (application) -> {
        new P2PTransactionsInterceptorProcessor().process(application);
    };

    /**
     * P2P transactions broadcast cross shards
     */
    public static AppTask INTERCEPT_XTRANSACTIONS = (application) -> {
        new P2PXTransactionsInterceptorProcessor().process(application);
    };

    /**
     * P2P receipts broadcast
     */
    public static AppTask INTERCEPT_RECEIPTS = (application) -> {
        new P2PReceiptInterceptorProcessor().process(application);
    };

    /**
     * P2P transactions broadcast
     */
//    public static AppTask BLOCK_ASSEMBLY_PROCESSOR = (application) -> {
//        new BlockAssemblyProcessor().process(application);
//    };


    /**
     * P2P block broadcast
     */
    public static AppTask INTERCEPT_BLOCKS = (application) -> {
        new P2PBlocksInterceptorProcessor().process(application);
    };

    /**
     * Init public and private keys
     */
    public static AppTask INITIALIZE_PUBLIC_PRIVATE_KEYS = (application) -> {
        new AccountInitializerProcessor().process(application);
    };

    /**
     * Init blockchain
     */
    public static AppTask INIT_BLOCKCHAIN = (application) -> {
        new BlockchainStarterProcessor().process(application);
    };

    /**
     * Init shard
     */
    public static AppTask INIT_SHARDING = (application) -> {
        new ShardStarterProcessor().process(application);
    };

    /**
     * Init blockchain
     */
    public static AppTask INIT_ACCOUNTS = (application) -> {
        new AccountsStarterProcessor().process(application);
    };


    /**
     * Init system synchronize
     */
    public static AppTask BLOCKCHAIN_SYNCRONIZATION = (application) -> {
        new SynchronizationBlockTask().process(application);
    };


    /**
     * Init system bootstrap
     */
    public static AppTask BLOCKCHAIN_BOOTSTRAP = (application) -> {
        new BootstrapBlockTask().process(application);
    };

    /**
     * Init request object mechanism
     */
    public static AppTask INIT_REQUEST_OBJECT = (application) -> {
        new P2PRequestObjectStarterProcessor().process(application);
    };

    /**
     * Init NTP client
     */
    public static AppTask NTP_CLIENT_INITIALIZER = (application) -> {
        new NtpClientInitializerProcessor().process(application);
    };


    public static AppTask CHRONOLOGY = (application) -> {
        new ChronologyBlockTask().process(application);
    };



}
