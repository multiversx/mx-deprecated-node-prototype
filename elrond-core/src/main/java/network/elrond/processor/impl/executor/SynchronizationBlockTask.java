package network.elrond.processor.impl.executor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.LocationType;
import network.elrond.processor.impl.AbstractBlockTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SynchronizationBlockTask extends AbstractBlockTask {
    private static final Logger logger = LogManager.getLogger(SynchronizationBlockTask.class);

    @Override
    protected void doProcess(Application application) {
        logger.traceEntry("params: {}", application);
        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();

        long timeStamp = 0;
        try {
            timeStamp = System.currentTimeMillis();

            AppServiceProvider.getBootstrapService().fetchNetworkBlockIndex(blockchain);

            logger.debug("Took {} ms to fetch max height with status {}", System.currentTimeMillis() - timeStamp,
                    AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.NETWORK, blockchain).getResponse());
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    @Override
    protected int getWaitingTime(){
        return 500;
    }
}

