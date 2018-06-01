package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.ThreadUtil;
import network.elrond.data.BootstrapService;
import network.elrond.data.ExecutionReport;
import network.elrond.data.LocationType;
import network.elrond.processor.AppTask;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;

public class BootstrappingProcessor implements AppTask {

    private Logger logger = LoggerFactory.getLogger(BootstrappingProcessor.class);

    private BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();


    @Override
    public void process(Application application) throws IOException {

        Thread threadProcess = new Thread(() -> {

            AppState state = application.getState();

            while (state.isStillRunning()) {


                if (state.isLock()) {
                    ThreadUtil.sleep(100);
                    continue;
                }

                state.setLock();

                synchronizeBlockchain(application);

                state.clearLock();

                logger.info("Nothing else to bootstrap! Waiting 5 seconds...");
                ThreadUtil.sleep(5000);

            }
        });
        threadProcess.start();
    }

    private void synchronizeBlockchain(Application application) {

        AppState state = application.getState();
        AppContext context = application.getContext();


        try {
            BigInteger remoteBlockIndex = bootstrapService.getMaxBlockSize(LocationType.NETWORK, state.getBlockchain());
            BigInteger localBlockIndex = bootstrapService.getMaxBlockSize(LocationType.LOCAL, state.getBlockchain());


            ExecutionReport exReport = new ExecutionReport();


            boolean shouldGenerateGenesis = context.isSeedNode() && (remoteBlockIndex.compareTo(BigInteger.ZERO) < 0);
            if (shouldGenerateGenesis) {

                bootstrapService.startFromScratch(application);
//
//                //if node is seeder and is first run
//                if (localBlockIndex.compareTo(BigInteger.ZERO) < 0) {
//                    //nothing defined, start from scratch
//                    exReport.combine(bootstrapService.startFromScratch(application));
//                } else {
//                    //only seeder can rebuild from disk or start from scratch
//                    if (context.getBootstrapType() == BootstrapType.START_FROM_SCRATCH) {
//                        exReport.combine(bootstrapService.startFromScratch(application));
//                    } else if (context.getBootstrapType() == BootstrapType.REBUILD_FROM_DISK) {
//                        exReport.combine(bootstrapService.rebuildFromDisk(application, localBlockIndex));
//                    } else {
//                        exReport.combine(new ExecutionReport().ko("Can not bootstrap! Unknown BootstrapType : " +
//                                context.getBootstrapType().toString() + "!"));
//                    }
//                }
//            } else {
//                //node is slave
            }

            boolean isBlocAvailable = remoteBlockIndex.compareTo(BigInteger.ZERO) >= 0;
            boolean isNewBlockRemote = remoteBlockIndex.compareTo(localBlockIndex) > 0;
            boolean isSyncRequired = isBlocAvailable && isNewBlockRemote;

            if (isSyncRequired) {
                //bootstrap
                ExecutionReport report = bootstrapService.bootstrap(application, localBlockIndex, remoteBlockIndex);
                exReport.combine(report);
            }

//            if ((localBlockIndex.compareTo(BigInteger.ZERO) >= 0) && (localBlockIndex.compareTo(remoteBlockIndex) > 0)
//                    && (remoteBlockIndex.compareTo(Util.BIG_INT_MIN_ONE) > 0)) {
//                exReport.combine(bootstrapService.rebuildFromDiskDeltaNoExec(application, localBlockIndex, remoteBlockIndex));
//            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ;
        }
    }


}

