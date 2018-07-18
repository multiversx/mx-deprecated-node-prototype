package network.elrond.processor.impl.executor;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.BootstrapType;
import network.elrond.data.LocationType;
import network.elrond.processor.impl.AbstractBlockTask;
import network.elrond.processor.impl.initialization.BlockchainStarterProcessor;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;

public class BootstrapBlockTask extends AbstractBlockTask {

    private static final Logger logger = LogManager.getLogger(BlockchainStarterProcessor.class);


    @Override
    protected void doProcess(Application application) {
        logger.traceEntry("params: {}", application);

        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        AppContext context = application.getContext();

        try {
            BigInteger remoteBlockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.NETWORK, blockchain);
            BigInteger localBlockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.LOCAL, blockchain);

            boolean isLeaderInShard = AppShardingManager.instance().isLeaderInShard(state);
            boolean isMissingGenesisBlock = (remoteBlockIndex.compareTo(BigInteger.ZERO) < 0 && localBlockIndex.compareTo(BigInteger.ZERO) < 0);
            boolean shouldGenerateGenesis = isLeaderInShard && isMissingGenesisBlock;
            if (!shouldGenerateGenesis) {
                logger.traceExit("should not generate genesis!");
                return;
            }

            BootstrapType bootstrapType = application.getContext().getBootstrapType();

            switch (bootstrapType) {
                case START_FROM_SCRATCH:
                    logger.trace("starting from genesis...");
                    AppServiceProvider.getBootstrapService().startFromGenesis(state, context);
                    break;
                case REBUILD_FROM_DISK:
                    logger.trace("starting from disk...");
                    AppServiceProvider.getBootstrapService().restoreFromDisk(localBlockIndex, state, context);
                    break;
                default:
                    RuntimeException ex = new RuntimeException("Not supported type" + bootstrapType);
                    logger.throwing(ex);
                    throw ex;
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }
    }


}

