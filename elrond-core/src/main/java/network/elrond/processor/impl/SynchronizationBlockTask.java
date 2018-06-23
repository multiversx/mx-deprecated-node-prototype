package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.ExecutionReport;
import network.elrond.data.SynchronizationRequirement;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SynchronizationBlockTask extends AbstractBlockTask {
    private static final Logger logger = LogManager.getLogger(SynchronizationBlockTask.class);

    @Override
    protected void doProcess(Application application) {
        logger.traceEntry("params: {}", application);
        AppState state = application.getState();
        Accounts accounts = state.getAccounts();
        Blockchain blockchain = state.getBlockchain();

        try {

            ExecutionReport exReport = new ExecutionReport();

            SynchronizationRequirement synchronizationRequirement = AppServiceProvider.getBootstrapService().getSynchronizationRequirement(blockchain);

            if (!synchronizationRequirement.isSyncRequired()) {
                logger.trace("is not sync required!");
                return;
            }

            logger.info(String.format("%s, Starting to synchronize > Current bloc index %d | remote block index %d",
                    application.getContext().getNodeName(), synchronizationRequirement.getLocalBlockIndex(),
                    synchronizationRequirement.getRemoteBlockIndex()));

            ExecutionReport report = AppServiceProvider.getBootstrapService().synchronize(synchronizationRequirement.getLocalBlockIndex(), synchronizationRequirement.getRemoteBlockIndex(), blockchain, accounts);
            exReport.combine(report);

            logger.debug("Sync result: {}", report);
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }
}

