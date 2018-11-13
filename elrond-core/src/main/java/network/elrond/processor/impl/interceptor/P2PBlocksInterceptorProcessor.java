package network.elrond.processor.impl.interceptor;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.Util;
import network.elrond.data.model.Block;
import network.elrond.p2p.model.P2PBroadcastChannelName;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P2PBlocksInterceptorProcessor extends AbstractChannelTask<Block> {

    private static final Logger logger = LogManager.getLogger(P2PBlocksInterceptorProcessor.class);

    @Override
    protected P2PBroadcastChannelName getChannelName() {
        return P2PBroadcastChannelName.BLOCK;
    }

    @Override
    protected void process(Block block, Application application) {
        logger.traceEntry("params: {} {}", block, application);
        Util.check(block != null, "block != null");

        String hash = AppServiceProvider.getSerializationService().getHashString(block);
        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        Accounts accounts = state.getAccounts();

        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        blockchainService.putLocal(hash, block, blockchain, BlockchainUnitType.BLOCK);
        blockchainService.putLocal(block.getNonce(), hash, blockchain, BlockchainUnitType.BLOCK_INDEX);

        AppServiceProvider.getExecutionService().processBlock(block, accounts, blockchain, state.getStatisticsManager());
        logger.info("Got new block with hash {}", hash);
    }
}
