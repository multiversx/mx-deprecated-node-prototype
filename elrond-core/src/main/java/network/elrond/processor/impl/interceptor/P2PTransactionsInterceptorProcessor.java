package network.elrond.processor.impl.interceptor;

import network.elrond.Application;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.application.AppState;
import network.elrond.blockchain.TransactionsPool;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P2PTransactionsInterceptorProcessor extends AbstractChannelTask<Transaction> {
    private static final Logger logger = LogManager.getLogger(P2PTransactionsInterceptorProcessor.class);

    @Override
    protected P2PBroadcastChannelName getChannelName() {
        return P2PBroadcastChannelName.TRANSACTION;
    }

    @Override
    protected void process(Transaction transaction, Application application) {
        logger.traceEntry("params: {} {}", transaction, application);
        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
        String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
        try {
            TransactionsPool pool = blockchain.getPool();

            if (pool.checkExists(hash)) {
                logger.trace("Transaction hash {} already processed/fetched!", transaction);
                logger.traceExit();
                return;
            }

            // This will retrieve transaction from network if required
            blockchainService.putLocal(hash, transaction, blockchain, BlockchainUnitType.TRANSACTION);

            pool.addTransaction(hash);
            logger.trace("Got new transaction with hash {}", hash);
        } catch (Exception ex) {
            logger.catching(ex);
        }

        logger.traceExit();
    }

    @Override
    protected int getWaitingTime() {
        return 5;
    }
}
