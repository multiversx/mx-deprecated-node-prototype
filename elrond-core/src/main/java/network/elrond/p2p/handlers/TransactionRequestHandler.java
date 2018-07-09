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
        try {
            Transaction transaction = AppServiceProvider.getBlockchainService().get(transactionHash, blockchain, BlockchainUnitType.TRANSACTION);
            return logger.traceExit(transaction);
        } catch (IOException | ClassNotFoundException e) {
            logger.catching(e);
            return logger.traceExit((Transaction) null);
        }
    }
}