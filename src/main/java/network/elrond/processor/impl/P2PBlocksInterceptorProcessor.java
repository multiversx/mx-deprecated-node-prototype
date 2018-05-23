package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Block;
import network.elrond.p2p.AppP2PManager;
import network.elrond.processor.AppProcessor;
import network.elrond.processor.AppProcessors;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class P2PBlocksInterceptorProcessor implements AppProcessor {

    private Logger logger = LoggerFactory.getLogger(AppProcessors.class);
    private static String CHANNEL_NAME = "BLOCKS";

    @Override
    public void process(Application application) throws IOException {


        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10000);

        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();

        Thread threadProcessBlockHashes = new Thread(() -> {

            while (state.isStillRunning()) {

                String hash = queue.poll();
                if (hash == null) {
                    continue;
                }

                try {

                    // This will retrieve block form network if required
                    Block block = AppServiceProvider.getBlockchainService().get(hash, blockchain, BlockchainUnitType.BLOCK);
                    if (block != null) {
                        logger.info("Got new block " + hash);
                    } else {
                        logger.info("Block not found !!!: " + hash);
                    }


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

            if (strPayload.startsWith("H:")) {
                strPayload = strPayload.substring(2);
                //pool.pushKey(strPayload);
                try {
                    queue.put(strPayload);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            //System.err.println(sender + " - " + request);
        });
    }
}
