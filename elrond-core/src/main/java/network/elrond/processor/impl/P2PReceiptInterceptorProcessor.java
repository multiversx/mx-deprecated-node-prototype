package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Receipt;
import network.elrond.p2p.P2PChannelName;
import network.elrond.processor.AppTasks;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2PReceiptInterceptorProcessor extends AbstractChannelTask<String> {

    private Logger logger = LoggerFactory.getLogger(AppTasks.class);

    @Override
    protected P2PChannelName getChannelName() {
        return P2PChannelName.RECEIPT;
    }

    @Override
    protected void process(String hash, Application application) {

        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        try {
            // This will retrieve receipt from network if required
            Receipt receipt = blockchainService.get(hash, blockchain, BlockchainUnitType.RECEIPT);

            if (receipt == null) {
                logger.info("Receipt not found !!!: " + hash);
                return;
            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }
}
