package network.elrond.p2p.handlers;

import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.p2p.P2PRequestMessage;
import network.elrond.p2p.RequestHandler;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockIndexRequestHandler implements RequestHandler<String, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(BlockIndexRequestHandler.class);

    @Override
    public String onRequest(AppState state, P2PRequestMessage data) {
        logger.traceEntry("params: {} {}", state, data);
        data.getKey();
        String blockHash = (String) data.getKey();
        Blockchain blockchain = state.getBlockchain();
        String blockIndex = AppServiceProvider.getBlockchainService().getLocal(blockHash, blockchain, BlockchainUnitType.BLOCK_INDEX);
        if (blockIndex == null) {
            logger.info("Replying to request: BLOCK_INDEX with hash {} not found", blockHash);
        } else {
            logger.info("Replying to request: BLOCK_INDEX with hash {} : {}", blockHash, blockIndex);
        }
        return logger.traceExit(blockIndex);
    }
}
