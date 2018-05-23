package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.p2p.AppP2PManager;
import network.elrond.application.AppState;
import network.elrond.data.Transaction;
import network.elrond.data.TransactionService;
import network.elrond.p2p.P2PConnection;
import network.elrond.processor.AppProcessor;
import network.elrond.processor.AppProcessors;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class P2PTransactionsInterceptorProcessor implements AppProcessor {

    private Logger logger = LoggerFactory.getLogger(AppProcessors.class);
    private static final String CHANNEL_NAME = "TRANSACTIONS";
    TransactionService ts = AppServiceProvider.getTransactionService();

    @Override
    public void process(Application application) throws IOException {

        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10000);

        AppState state = application.getState();
        P2PConnection connection = state.getConnection();
        Blockchain blockchain = state.getBlockchain();

        Thread threadProcessTxHashes = new Thread(() -> {

            while (state.isStillRunning()) {
                String hash = queue.poll();
                if (hash == null) {
                    continue;
                }

                try {
                    // This will retrieve tx from network if required
                    Transaction tx = AppServiceProvider.getBlockchainService().get(hash, blockchain, BlockchainUnitType.TRANSACTION);

                    if (tx != null) {
                        logger.info("Got new tx " + hash);
                    } else {
                        logger.info("Tx not found !!!: " + hash);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
        threadProcessTxHashes.start();


        AppP2PManager.instance().subscribeToChannel(application, CHANNEL_NAME, (sender, request) -> {
            if (request == null) {
                return;
            }
            String strPayload = request.getPayload().toString();
            //test if it's a tx hash
            if (strPayload.startsWith("H:")) {
                strPayload = strPayload.substring(2);
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
