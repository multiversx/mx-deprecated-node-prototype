package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.ExecutionReport;
import network.elrond.data.LocationType;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class SynchronizationBlockTask extends AbstractBlockTask {

    private Logger logger = LoggerFactory.getLogger(SynchronizationBlockTask.class);


    @Override
    protected void doProcess(Application application) {

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
                return;
            }

            ExecutionReport report = AppServiceProvider.getBootstrapService().synchronize(localBlockIndex, remoteBlockIndex, blockchain, accounts);
            exReport.combine(report);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}

