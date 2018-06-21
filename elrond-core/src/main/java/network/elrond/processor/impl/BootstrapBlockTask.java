package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.chronology.NTPClient;
import network.elrond.data.BootstrapType;
import network.elrond.data.LocationType;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;

public class BootstrapBlockTask extends AbstractBlockTask {

    private static final Logger logger = LogManager.getLogger(BlockchainStarterProcessor.class);

    @Override
    protected void doProcess(Application application) {
        logger.traceEntry("params: {}", application);

        AppState state = application.getState();
        Accounts accounts = state.getAccounts();
        Blockchain blockchain = state.getBlockchain();
        AppContext context = application.getContext();
        NTPClient ntpClient = state.getNtpClient();

        try {
            BigInteger remoteBlockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.NETWORK, blockchain);
            BigInteger localBlockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.LOCAL, blockchain);

            boolean isSeedNode = context.isSeedNode();
            boolean isMissingGenesisBlock = remoteBlockIndex.compareTo(BigInteger.ZERO) < 0;
            boolean shouldGenerateGenesis = isSeedNode && isMissingGenesisBlock;
            if (!shouldGenerateGenesis) {
                logger.traceExit("should not generate genesis!");
                return;
            }

            BootstrapType bootstrapType = application.getContext().getBootstrapType();

            switch (bootstrapType) {
                case START_FROM_SCRATCH:
                    logger.trace("starting from genesis...");
                    AppServiceProvider.getBootstrapService().startFromGenesis(accounts, blockchain, context, state.getNtpClient());
                    break;
                case REBUILD_FROM_DISK:
                    logger.trace("starting from disk...");
                    AppServiceProvider.getBootstrapService().restoreFromDisk(localBlockIndex, accounts, blockchain, context, state.getNtpClient());
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

