package network.elrond.p2p.handlers;

import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PRequestMessage;
import network.elrond.p2p.RequestHandler;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class TransactionRequestHandler implements RequestHandler<Transaction, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(TransactionRequestHandler.class);

    @Override
    public Transaction onRequest(AppState state, P2PRequestMessage data) {
        logger.traceEntry("params: {} {}", state, data);
        data.getKey();
        String transactionHash = (String) data.getKey();
        Blockchain blockchain = state.getBlockchain();
        Transaction transaction = AppServiceProvider.getBlockchainService().getLocal(transactionHash, blockchain, BlockchainUnitType.TRANSACTION);
        if (transaction == null) {
            logger.info("requested transaction with hash {} not found", transactionHash);
        } else {
            logger.info("requested transaction with hash {} : {}", transactionHash, transaction);
        }

        return logger.traceExit(transaction);
    }
}