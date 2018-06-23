package network.elrond.processor.impl.executor;

import network.elrond.Application;
import network.elrond.TimeWatch;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.ThreadUtil;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.AppBlockManager;
import network.elrond.data.LocationType;
import network.elrond.p2p.P2PChannelName;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.sharding.AppShardingManager;
import network.elrond.sharding.Shard;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Collect new transactions and put them into new block
 */
public class BlockAssemblyProcessor extends AbstractChannelTask<String> {
    private static final Logger logger = LogManager.getLogger(BlockAssemblyProcessor.class);

    @Override
    protected P2PChannelName getChannelName() {
        return P2PChannelName.TRANSACTION;
    }

    @Override
    protected void process(ArrayBlockingQueue<String> queue, Application application) {
        logger.traceEntry("params: {} {}", queue, application);

        ThreadUtil.sleep(4000);
        AppState state = application.getState();
        Shard shard = state.getShard();

        boolean isLeaderInShard = AppShardingManager.instance().isLeaderInShard(state);
        if (!isLeaderInShard) {
            logger.info("Node is not leader in shard {}", shard);
            return;
        }

        logger.info("Node is leader in shard {}", shard);

        if (state.isLock()) {
            // If sync is running stop
            logger.info("Can't execute, state locked!");
            return;
        }

        if (state.getBlockchain().getCurrentBlock() == null) {
            // Require synchronize
            logger.info("Can't execute, synchronize required!");
            return;
        }

        int size = queue.size();
        TimeWatch watch = TimeWatch.start();

        state.setLock();
        proposeBlock(queue, application);
        state.clearLock();


        long time = watch.time(TimeUnit.MILLISECONDS);
        long tps = (time > 0) ? ((size * 1000) / time) : 0;
        logger.info(" ###### Executed " + size + " transactions in " + time + "ms  TPS:" + tps + "   ###### ");

        logger.traceExit();
    }


    private void proposeBlock(ArrayBlockingQueue<String> queue, Application application) {
        logger.traceEntry("params: {} {}", queue, application);

        AppState state = application.getState();

        List<String> hashes = new ArrayList<>(queue);
        queue.clear();

        if (hashes.isEmpty()) {
            logger.info("Can't execute, no transaction!");
            return;
        }

        AppContext context = application.getContext();
        PrivateKey privateKey = context.getPrivateKey();

        AppBlockManager.instance().generateAndBroadcastBlock(hashes, privateKey, state);

        logger.traceExit();
    }

    private void removeProcessedTransactions(ArrayBlockingQueue<String> queue, Application application) {
        logger.traceEntry("params: {} {}", queue, application);

        List<String> hashes = new ArrayList<>(queue);
        Blockchain blockchain = application.getState().getBlockchain();
        BigInteger localBlockIndex;
        List<String> lastBlockHashes = new ArrayList<>();

        try {
            localBlockIndex = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.LOCAL, blockchain);

            // TODO: take the number of blocks to check from a config file
            BigInteger earliestBlockToCheck = (localBlockIndex.subtract(BigInteger.valueOf(50)).compareTo(BigInteger.ZERO) < 0) ?
                    BigInteger.ZERO : localBlockIndex.subtract(BigInteger.valueOf(50));

            for (BigInteger i = localBlockIndex; i.compareTo(earliestBlockToCheck) >= 0; i = i.subtract(BigInteger.ONE)) {
                lastBlockHashes.add(AppServiceProvider.getBootstrapService().getBlockHashFromIndex(i, blockchain));
            }

            for (String txHash : hashes) {
                String blockHash = AppServiceProvider.getBlockchainService().get(txHash, blockchain, BlockchainUnitType.TRANSACTION_BLOCK);

                boolean transactionLinkedToLocalBlocks = (blockHash != null) && lastBlockHashes.contains(blockHash);

                if (transactionLinkedToLocalBlocks) {
                    queue.remove(txHash);
                }
            }
        } catch (Exception e) {
            logger.catching(e);
        }
        logger.traceExit();
    }

    @Override
    protected void process(String hash, Application application) {

    }
}
