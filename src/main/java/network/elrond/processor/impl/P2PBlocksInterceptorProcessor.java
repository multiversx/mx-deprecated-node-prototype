package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.p2p.AppP2PManager;
import network.elrond.application.AppState;
import network.elrond.data.Block;
import network.elrond.data.SynchronizedPool;
import network.elrond.p2p.P2PConnection;
import network.elrond.processor.AppProcessor;
import network.elrond.processor.AppProcessors;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class P2PBlocksInterceptorProcessor implements AppProcessor {

    private Logger logger = LoggerFactory.getLogger(AppProcessors.class);

    private static String CHANNEL_NAME = "BLOCKS";

    @Override
    public void process(Application application) throws IOException {


        AppState state = application.getState();
        SynchronizedPool<String, Block> pool = state.syncDataBlk;
        Thread threadProcessBlockHashes = new Thread(() -> {

            while (state.isStillRunning()) {
                String strHash = pool.popKey();

                if (strHash == null) {
                    continue;
                }

                if (pool.isObjectInPool(strHash)) {
                    continue;
                }
                try {

                    P2PConnection connection = state.getConnection();
                    Object objData = AppServiceProvider.getP2PObjectService().get(connection, strHash);


                    if (objData != null) {
                        pool.addObjectInPool(strHash, Block.createInstance(objData.toString()));
                        logger.info("Got blk hash: " + strHash);
                    }

                    logger.info("Blk pool size: " + pool.size());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
        threadProcessBlockHashes.start();


        AppP2PManager.instance().subscribeToChannel(application, CHANNEL_NAME, (sender, request) -> {
            if (request == null) {
                return;
            }
            String strPayload = request.getPayload().toString();
            //test if it's a blk hash
            if (strPayload.startsWith("H:")) {
                strPayload = strPayload.substring(2);
                pool.pushKey(strPayload);
            }

            //System.err.println(sender + " - " + request);
        });
    }
}
