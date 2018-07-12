package network.elrond.p2p.handlers;

import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Receipt;
import network.elrond.data.SecureObject;
import network.elrond.p2p.P2PRequestMessage;
import network.elrond.p2p.RequestHandler;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ReceiptRequestHandler implements RequestHandler<Receipt, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(ReceiptRequestHandler.class);

    @Override
    public Receipt onRequest(AppState state, P2PRequestMessage data) {
        logger.traceEntry("params: {} {}", state, data);
        data.getKey();
        String receiptHash = (String) data.getKey();
        Blockchain blockchain = state.getBlockchain();
        try {
            Receipt receipt = (Receipt) ((SecureObject) AppServiceProvider.getBlockchainService().get(receiptHash, blockchain, BlockchainUnitType.RECEIPT)).getObject();
            return logger.traceExit(receipt);
        } catch (IOException | ClassNotFoundException e) {
            logger.catching(e);
            return logger.traceExit((Receipt) null);
        }
    }
}
