package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.application.AppState;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PChannelName;
import network.elrond.processor.AppTasks;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2PTransactionsInterceptorProcessor extends AbstractChannelTask<String> {

    private Logger logger = LoggerFactory.getLogger(AppTasks.class);

    @Override
    protected P2PChannelName getChannelName() {
        return P2PChannelName.TRANSACTION;
    }

    @Override
    protected void process(String hash, Application application) {

        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        try {

            // This will retrieve transaction from network if required
            Transaction transaction = blockchainService.get(hash, blockchain, BlockchainUnitType.TRANSACTION);

            if (transaction == null) {
                logger.info("Transaction not found !!!: " + hash);
                return;
            }

            //logger.info("Received new transaction " + hash);

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }
}
