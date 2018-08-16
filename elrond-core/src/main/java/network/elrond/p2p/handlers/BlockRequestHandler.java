package network.elrond.p2p.handlers;

import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Block;
import network.elrond.p2p.P2PRequestMessage;
import network.elrond.p2p.RequestHandler;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockRequestHandler implements RequestHandler<Block, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(BlockRequestHandler.class);

    @Override
    public Block onRequest(AppState state, P2PRequestMessage data) {
        logger.traceEntry("params: {} {}", state, data);
        data.getKey();
        String blockHash = (String) data.getKey();
        Blockchain blockchain = state.getBlockchain();
        Block block = AppServiceProvider.getBlockchainService().getLocal(blockHash, blockchain, BlockchainUnitType.BLOCK);
        if (block == null) {
            logger.info("Replying to request: BLOCK with hash {} not found", blockHash);
        } else {
            logger.info("Replying to request: BLOCK with hash {} : {}", blockHash, block);
        }
        return logger.traceExit(block);
    }
}
