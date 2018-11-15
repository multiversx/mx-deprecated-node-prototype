package network.elrond.p2p.handlers;

import network.elrond.application.AppState;
import network.elrond.p2p.RequestHandler;
import network.elrond.p2p.model.P2PRequestMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;

public class BlockHeightRequestHandler implements RequestHandler<BigInteger, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(BlockHeightRequestHandler.class);

    @Override
    public BigInteger onRequest(AppState state, P2PRequestMessage data) {
        logger.traceEntry("params: {} {}", state, data);

        logger.info("Replying to request: BLOCK_HEIGHT with value {}", state.getBlockchain().getNetworkHeight());
        return logger.traceExit(state.getBlockchain().getNetworkHeight());
    }
}
