package network.elrond.processor.impl.interceptor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.ObjectUtil;
import network.elrond.data.Transaction;
import network.elrond.data.TransferDataBlock;
import network.elrond.p2p.P2PBroadcastChannel;
import network.elrond.p2p.P2PBroadcastChannelName;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class P2PXTransactionsInterceptorProcessor extends AbstractChannelTask<TransferDataBlock<Transaction>> {
    private static final Logger logger = LogManager.getLogger(P2PXTransactionsInterceptorProcessor.class);

    @Override
    protected P2PBroadcastChannelName getChannelName() {
        return P2PBroadcastChannelName.XTRANSACTION_BLOCK;
    }

    @Override
    protected void process(TransferDataBlock<Transaction> transactionBlock, Application application) {
        logger.traceEntry("params: {} {}", transactionBlock, application);
        AppState state = application.getState();
        Blockchain blockchain = state.getBlockchain();
        List<Transaction> transactionList = transactionBlock.getDataList();

        try {

            boolean isLeaderInShard = AppShardingManager.instance().isLeaderInShard(state);
            if (!isLeaderInShard) {
                return;
            }

            Shard shard = state.getShard();
            Shard receiverShard = transactionBlock.getDataList().get(0).getReceiverShard();
            boolean isCrossShard = !ObjectUtil.isEqual(shard, receiverShard);
            if (isCrossShard) {
                return;
            }

            P2PBroadcastChannel channel = state.getChannel(P2PBroadcastChannelName.TRANSACTION);

            for (Transaction transaction : transactionList) {
                String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
                AppServiceProvider.getBlockchainService().putLocal(hash, transaction, blockchain, BlockchainUnitType.TRANSACTION);

                boolean transactionNew = !blockchain.getPool().checkExists(hash);

                if (transactionNew) {
                    AppServiceProvider.getP2PBroadcastService().publishToChannel(channel, transaction, shard.getIndex());
                }
            }
            logger.trace("Got new xtransactions {}", transactionList);

        } catch (Exception ex) {
            logger.catching(ex);
        }

        logger.traceExit();
    }
}
