package network.elrond.processor.impl.interceptor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.FutureUtil;
import network.elrond.core.ThreadUtil;
import network.elrond.data.Receipt;
import network.elrond.data.SecureObject;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P2PReceiptInterceptorProcessor extends AbstractChannelTask<String> {

    private static final Logger logger = LogManager.getLogger(P2PReceiptInterceptorProcessor.class);

    @Override
    protected P2PBroadcastChannelName getChannelName() {
        return P2PBroadcastChannelName.RECEIPT;
    }

    @Override
    protected void process(String hash, Application application) {
        logger.traceEntry("params: {} {}", hash, application);
        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        try {

            SecureObject<Receipt> secureReceipt = FutureUtil.get(() -> {
                SecureObject<Receipt> result;
                do {
                    result = blockchainService.get(hash, blockchain, BlockchainUnitType.RECEIPT);
                    ThreadUtil.sleep(200);
                } while (result == null);
                return result;
            }, 60L);


            if (secureReceipt == null) {
                logger.warn("Receipt with hash {} was not found!", hash);
                logger.traceExit();
                return;
            }

            logger.trace("Got new receipt with hash {}", hash);

        } catch (Exception ex) {
            logger.catching(ex);
        }
        logger.traceExit();
    }
}
