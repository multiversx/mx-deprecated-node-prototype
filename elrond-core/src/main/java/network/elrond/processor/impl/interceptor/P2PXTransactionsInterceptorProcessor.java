package network.elrond.processor.impl.interceptor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.core.ObjectUtil;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PChannelName;
import network.elrond.p2p.P2PConnection;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P2PXTransactionsInterceptorProcessor extends AbstractChannelTask<Transaction> {
    private static final Logger logger = LogManager.getLogger(P2PXTransactionsInterceptorProcessor.class);

    @Override
    protected P2PChannelName getChannelName() {
        return P2PChannelName.XTRANSACTION;
    }

    @Override
    protected void process(Transaction transaction, Application application) {
        logger.traceEntry("params: {} {}", transaction, application);
        AppState state = application.getState();
        try {

            AppShardingManager.instance().calculateAndSetRole(state);
            boolean isLeaderInShard = AppShardingManager.instance().isLeader();
            if (!isLeaderInShard) {
                return;
            }

            Shard shard = state.getShard();
            Shard receiverShard = transaction.getReceiverShard();
            boolean isCrossShard = !ObjectUtil.isEqual(shard, receiverShard);
            if (isCrossShard) {
                return;
            }


            String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
            P2PConnection connection = state.getConnection();
            AppServiceProvider.getP2PObjectService().put(connection, hash, transaction);

            P2PBroadcastChanel channel = state.getChanel(P2PChannelName.TRANSACTION);
            AppServiceProvider.getP2PBroadcastService().publishToChannel(channel, hash);


            logger.trace("Got new xtransaction {}", transaction);


        } catch (Exception ex) {
            logger.catching(ex);
        }

        logger.traceExit();
    }
}
