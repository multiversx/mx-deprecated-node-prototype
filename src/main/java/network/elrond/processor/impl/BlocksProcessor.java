package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.data.Block;
import network.elrond.data.SynchronizedPool;
import network.elrond.processor.AppProcessor;
import network.elrond.processor.AppProcessors;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BlocksProcessor implements AppProcessor {
    private Logger logger = LoggerFactory.getLogger(AppProcessors.class);

    public void process(Application application) throws IOException {
        AppState state = application.getState();
        SynchronizedPool<String, Block> pool = state.syncDataBlk;


        Thread threadProcessBlocks = new Thread(() -> {

            while (state.isStillRunning()) {
                //block solving
                AppServiceProvider.getBlockService().solveBlocks(state);




            }
        });
        threadProcessBlocks.start();



    }
}
