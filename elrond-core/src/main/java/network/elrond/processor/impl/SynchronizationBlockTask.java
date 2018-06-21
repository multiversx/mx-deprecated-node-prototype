package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.ExecutionReport;
import network.elrond.data.LocationType;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;

public class SynchronizationBlockTask extends AbstractBlockTask {
    private static final Logger logger = LogManager.getLogger(SynchronizationBlockTask.class);

    @Override
    protected void doProcess(Application application) {
        logger.traceEntry("params: {}", application);
        AppState state = application.getState();
        Accounts accounts = state.getAccounts();
        Blockchain blockchain = state.getBlockchain();

        try {

            BigInteger remoteBlockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.NETWORK, blockchain);
            BigInteger localBlockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.LOCAL, blockchain);

            ExecutionReport exReport = new ExecutionReport();

            boolean isBlocAvailable = remoteBlockIndex.compareTo(BigInteger.ZERO) >= 0;
            boolean isNewBlockRemote = remoteBlockIndex.compareTo(localBlockIndex) > 0;
            boolean isSyncRequired = isBlocAvailable && isNewBlockRemote;

            logger.info("Current bloc index " + localBlockIndex + " | remote block index " + remoteBlockIndex);

            if (!isSyncRequired) {
                logger.trace("is not sync required!");
                return;
            }

            logger.trace("Starting to synchronize...");
            ExecutionReport report = AppServiceProvider.getBootstrapService().synchronize(localBlockIndex, remoteBlockIndex, blockchain, accounts);
            exReport.combine(report);

            logger.trace("Sync result: {}", report);
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }


}

