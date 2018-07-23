package network.elrond.processor.impl.interceptor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.FutureUtil;
import network.elrond.core.ThreadUtil;
import network.elrond.data.Receipt;
import network.elrond.data.TransferDataBlock;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class P2PReceiptInterceptorProcessor extends AbstractChannelTask<TransferDataBlock<String>> {

    private static final Logger logger = LogManager.getLogger(P2PReceiptInterceptorProcessor.class);

    @Override
    protected P2PBroadcastChannelName getChannelName() {
        return P2PBroadcastChannelName.RECEIPT_BLOCK;
    }

    @Override
    protected void process(TransferDataBlock<String> receiptBlock, Application application) {
        logger.traceEntry("params: {} {}", receiptBlock, application);
        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
        List<String> receiptHashList = receiptBlock.getDataList();

//        receiptHashList.stream().parallel().forEach(hash -> {
        for (String hash : receiptHashList) {
            try {
                Receipt receipt = FutureUtil.get(() -> {
                    Receipt receiptDHT;
                    do {
                        receiptDHT = blockchainService.get(hash, blockchain, BlockchainUnitType.RECEIPT);
                        ThreadUtil.sleep(100);
                    } while (receiptDHT == null);
                    return receiptDHT;
                }, 60L);

                if (receipt == null) {
                    logger.warn("Receipt with hash {} was not found!", hash);
                }
            } catch (Exception ex) {
                logger.catching(ex);
            }
        }
//        });
        logger.traceExit();
    }
}
