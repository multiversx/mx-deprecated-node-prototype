package network.elrond.processor.impl.executor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.model.ExecutionReport;
import network.elrond.data.model.SyncState;
import network.elrond.processor.impl.AbstractBlockTask;
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
        Blockchain blockchain = state.getBlockchain();

        try {

            SyncState syncState = AppServiceProvider.getBootstrapService().getSyncState(blockchain);

            if (!syncState.isSyncRequired()) {
                logger.trace("is not sync required!");
                return;
            }

            BigInteger localBlockIndex = syncState.getLocalBlockIndex();
            BigInteger remoteBlockIndex = syncState.getRemoteBlockIndex();

            logger.info("{}, Starting to synchronize > Current bloc index {} | remote block index {}",
                    application.getContext().getNodeName(), localBlockIndex,
                    remoteBlockIndex);

            ExecutionReport report = AppServiceProvider.getBootstrapService().synchronize(localBlockIndex, remoteBlockIndex, state);

            logger.debug("Sync result: {}", report);
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }
}

