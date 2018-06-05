package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.BootstrapType;
import network.elrond.data.LocationType;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class BootstrapBlockTask extends AbstractBlockTask {

    private Logger logger = LoggerFactory.getLogger(BootstrapBlockTask.class);


    @Override
    protected void doProcess(Application application) {

        AppState state = application.getState();
        Accounts accounts = state.getAccounts();
        Blockchain blockchain = state.getBlockchain();
        AppContext context = application.getContext();

        try {

            BigInteger remoteBlockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.NETWORK, blockchain);
            BigInteger localBlockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.LOCAL, blockchain);

            boolean isSeedNode = context.isSeedNode();
            boolean isMissingGenesisBlock = remoteBlockIndex.compareTo(BigInteger.ZERO) < 0;
            boolean shouldGenerateGenesis = isSeedNode && isMissingGenesisBlock;
            if (!shouldGenerateGenesis) {
                return;
            }

            BootstrapType bootstrapType = application.getContext().getBootstrapType();

            switch (bootstrapType) {
                case START_FROM_SCRATCH:
                    AppServiceProvider.getBootstrapService().startFromGenesis(accounts, blockchain, context);
                    break;
                case REBUILD_FROM_DISK:
                    AppServiceProvider.getBootstrapService().restoreFromDisk(localBlockIndex, accounts, blockchain);
                    break;
                default:
                    throw new RuntimeException("Not supported type" + bootstrapType);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}

