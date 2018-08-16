package network.elrond.processor.impl.interceptor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.data.Receipt;
import network.elrond.data.TransferDataBlock;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class P2PReceiptInterceptorProcessor extends AbstractChannelTask<TransferDataBlock<Receipt>> {

    private static final Logger logger = LogManager.getLogger(P2PReceiptInterceptorProcessor.class);

    @Override
    protected P2PBroadcastChannelName getChannelName() {
        return P2PBroadcastChannelName.RECEIPT_BLOCK;
    }

    @Override
    protected void process(TransferDataBlock<Receipt> receiptBlock, Application application) {
        logger.traceEntry("params: {} {}", receiptBlock, application);
        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        List<Receipt> receiptList = receiptBlock.getDataList();

        receiptList.stream().parallel().forEach(receipt -> {
            if (receipt == null) {
                logger.warn("Null receiptreceived!");
            } else {
                String receiptHash = AppServiceProvider.getSerializationService().getHashString(receipt);
                String transactionHash = receipt.getTransactionHash();
                AppServiceProvider.getBlockchainService().putLocal(receiptHash, receipt, blockchain, BlockchainUnitType.RECEIPT);
                AppServiceProvider.getBlockchainService().putLocal(transactionHash, receiptHash, blockchain, BlockchainUnitType.TRANSACTION_RECEIPT);
            }
        });
        logger.traceExit();
    }
}
