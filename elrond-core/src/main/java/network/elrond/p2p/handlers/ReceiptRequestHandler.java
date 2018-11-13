package network.elrond.p2p.handlers;

import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.model.Receipt;
import network.elrond.p2p.RequestHandler;
import network.elrond.p2p.model.P2PRequestMessage;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReceiptRequestHandler implements RequestHandler<Receipt, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(ReceiptRequestHandler.class);

    @Override
    public Receipt onRequest(AppState state, P2PRequestMessage data) {
        logger.traceEntry("params: {} {}", state, data);
        data.getKey();
        String receiptHash = (String) data.getKey();
        Blockchain blockchain = state.getBlockchain();
        Receipt receipt = AppServiceProvider.getBlockchainService().getLocal(receiptHash, blockchain, BlockchainUnitType.RECEIPT);
        if (receipt == null) {
            logger.info("Replying to request: RECEIPT with hash {} not found", receiptHash);
        } else {
            logger.info("Replying to request: RECEIPT with hash {} : {}", receiptHash, receipt);
        }
        return logger.traceExit(receipt);
    }
}
