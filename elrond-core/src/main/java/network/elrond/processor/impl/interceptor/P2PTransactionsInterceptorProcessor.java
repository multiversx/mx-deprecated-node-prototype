package network.elrond.processor.impl.interceptor;

import network.elrond.Application;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.application.AppState;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P2PTransactionsInterceptorProcessor extends AbstractChannelTask<String> {
    private static final Logger logger = LogManager.getLogger(P2PTransactionsInterceptorProcessor.class);

    @Override
    protected P2PBroadcastChannelName getChannelName() {
        return P2PBroadcastChannelName.TRANSACTION;
    }

    @Override
    protected void process(String hash, Application application) {
        logger.traceEntry("params: {} {}", hash, application);
        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        try {

            // This will retrieve transaction from network if required
            Transaction transaction = blockchainService.get(hash, blockchain, BlockchainUnitType.TRANSACTION);
            synchronized (state.lockerTransactionPool) {
                boolean isHashPresent = blockchain.getTransactionsProcessed().checkExists(hash);

                if (!isHashPresent) {
                    state.addTransactionToPool(hash);
                }
            }

            if (transaction == null) {
                logger.warn("Transaction with hash {} was not found!", hash);
                return;
            }

            logger.trace("Got new transaction with hash {}", hash);
        } catch (Exception ex) {
            logger.catching(ex);
        }

        logger.traceExit();
    }

    @Override
    protected int getWaitingTime() {
        return 100;
    }
}
