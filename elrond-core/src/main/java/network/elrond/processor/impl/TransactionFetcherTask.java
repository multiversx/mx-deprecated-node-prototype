package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Transaction;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TransactionFetcherTask extends AbstractBlockTask {
    private static final Logger logger = LogManager.getLogger(TransactionFetcherTask.class);

    @Override
    protected void doProcess(Application application) {
        logger.traceEntry("params: {}", application);
        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();

        List<String> hashes = new ArrayList<>(blockchain.getTransactionPool());

        try {
            List<Transaction> transactions = AppServiceProvider.getBlockchainService().getAll(hashes, blockchain, BlockchainUnitType.TRANSACTION);
        } catch (Exception ex){
            logger.throwing(ex);
        }
        logger.traceExit();
    }
}