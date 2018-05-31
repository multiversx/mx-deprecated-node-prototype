package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.AccountsContext;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.blockchain.SettingsType;
import network.elrond.core.Util;
import network.elrond.crypto.PublicKey;
import network.elrond.data.*;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PObjectService;
import network.elrond.processor.AppTask;
import network.elrond.processor.AppTasks;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;

public class BootstrappingProcessor implements AppTask {

    private Logger logger = LoggerFactory.getLogger(AppTasks.class);

    private BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();
    private BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
    private BlockchainService appPersistanceService = AppServiceProvider.getAppPersistanceService();
    private TransactionService transactionService = AppServiceProvider.getTransactionService();
    private P2PObjectService p2PObjectService = AppServiceProvider.getP2PObjectService();
    private SerializationService serializationService = AppServiceProvider.getSerializationService();

    @Override
    public void process(Application application) throws IOException {
        AppState state = application.getState();

        AppContext context = application.getContext();

        Thread threadProcess = new Thread(() -> {

            BigInteger maxBlkHeightNetw = Util.BIG_INT_MIN_ONE;
            BigInteger maxBlkHeightLocal = Util.BIG_INT_MIN_ONE;

            while (state.isStillRunning()) {
                maxBlkHeightNetw = Util.BIG_INT_MIN_ONE;
                maxBlkHeightLocal = Util.BIG_INT_MIN_ONE;

                if (state.isCreatingBlock()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                try {
                    maxBlkHeightNetw = bootstrapService.getMaxBlockSize(LocationType.NETWORK, state.getBlockchain());
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }

                try {
                    maxBlkHeightLocal = bootstrapService.getMaxBlockSize(LocationType.LOCAL, state.getBlockchain());
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }

                ExecutionReport exReport = new ExecutionReport();

                state.setBootstrapping(true);

                if (context.isSeedNode() && (maxBlkHeightNetw.compareTo(BigInteger.ZERO) < 0)){
                    //if node is seeder and is first run
                    if (maxBlkHeightLocal.compareTo(BigInteger.ZERO) < 0) {
                        //nothing defined, start from scratch
                        exReport.combine(bootstrapService.startFromScratch(application));
                    } else {
                        //only seeder can rebuild from disk or start from scratch
                        if (context.getBootstrapType() == BootstrapType.START_FROM_SCRATCH) {
                            exReport.combine(bootstrapService.startFromScratch(application));
                        } else if (context.getBootstrapType() == BootstrapType.REBUILD_FROM_DISK) {
                            exReport.combine(bootstrapService.rebuildFromDisk(application, maxBlkHeightLocal));
                        } else {
                            exReport.combine(new ExecutionReport().ko("Can not bootstrap! Unknown BootstrapType : " +
                                    context.getBootstrapType().toString() + "!"));
                        }
                    }
                } else {
                    //node is slave
                }

                if ((maxBlkHeightNetw.compareTo(BigInteger.ZERO) >= 0) && (maxBlkHeightNetw.compareTo(maxBlkHeightLocal) > 0)){
                    //bootstrap
                    exReport.combine(bootstrapService.bootstrap(application, maxBlkHeightLocal, maxBlkHeightNetw));
                }

                if ((maxBlkHeightLocal.compareTo(BigInteger.ZERO) >= 0) && (maxBlkHeightLocal.compareTo(maxBlkHeightNetw) > 0)
                        && (maxBlkHeightNetw.compareTo(Util.BIG_INT_MIN_ONE) > 0)){
                    exReport.combine(bootstrapService.rebuildFromDiskDeltaNoExec(application, maxBlkHeightLocal, maxBlkHeightNetw));
                }

                state.setBootstrapping(false);

                logger.info("Nothing else to bootstrap! Waiting 5 seconds...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        threadProcess.start();
    }









}

