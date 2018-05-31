package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.data.BootstrapService;
import network.elrond.data.BootstrapType;
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

        BigInteger maxBlkHeightNetw = Util.BIG_INT_MIN_ONE;
        BigInteger maxBlkHeightLocal = Util.BIG_INT_MIN_ONE;

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


        if (context.isSeedNode() && (maxBlkHeightNetw.compareTo(BigInteger.ZERO) < 0)) {
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

        if ((maxBlkHeightNetw.compareTo(BigInteger.ZERO) >= 0) && (maxBlkHeightNetw.compareTo(maxBlkHeightLocal) > 0)) {
            //bootstrap
            exReport.combine(bootstrapService.bootstrap(application, maxBlkHeightLocal, maxBlkHeightNetw));
        }

        if ((maxBlkHeightLocal.compareTo(BigInteger.ZERO) >= 0) && (maxBlkHeightLocal.compareTo(maxBlkHeightNetw) > 0)
                && (maxBlkHeightNetw.compareTo(Util.BIG_INT_MIN_ONE) > 0)) {
            exReport.combine(bootstrapService.rebuildFromDiskDeltaNoExec(application, maxBlkHeightLocal, maxBlkHeightNetw));
        }
    }


}

