package network.elrond.consensus.handlers;

import network.elrond.TimeWatch;
import network.elrond.application.AppState;
import network.elrond.benchmark.Statistic;
import network.elrond.blockchain.TransactionsPool;
import network.elrond.chronology.SubRound;
import network.elrond.consensus.ConsensusState;
import network.elrond.core.EventHandler;
import network.elrond.core.ObjectUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.AppBlockManager;
import network.elrond.data.Block;
import network.elrond.data.BlockUtil;
import network.elrond.sharding.AppShardingManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AssemblyBlockHandler implements EventHandler<SubRound> {
    private static final Logger logger = LogManager.getLogger(AssemblyBlockHandler.class);

    public void onEvent(AppState state, SubRound data) {
        logger.traceEntry("params: {} {}",state, data);

        Util.check(state != null, "state is null while trying to get full nodes list!");

        if (!isLeader(state)) {
            logger.info("Round: {}, subRound: {}> Not this node's turn to process ...", data.getRound().getIndex(), data.getRoundState().name());
            logger.traceExit();
            return;
        }
        logger.info("Round: {}, subRound: {}> This node will assemble block.", data.getRound().getIndex(), data.getRoundState().name());

        if (state.getBlockchain().getCurrentBlock() == null) {
            // Require synchronize
            logger.info("Round: {}, subRound: {}> Can't execute, synchronize required!", data.getRound().getIndex(), data.getRoundState().name());
            logger.traceExit();
            return;
        }

        proposeBlock(state, data);

        logger.traceExit();
    }


    private void proposeBlock(AppState state, SubRound data) {
        logger.traceEntry("params: {}", state);

        TransactionsPool pool = state.getPool();
        List<String> hashes = pool.getTransactions();
        if (hashes.isEmpty()) {
            logger.info("Round: {}, subRound: {}> Can't execute, no transactions!",
                    data.getRound().getIndex(), data.getRoundState().name());
            state.getStatisticsManager().addStatistic(new Statistic(0));
            return;
        }

        TimeWatch watch = TimeWatch.start();
        int size = 0;

        PrivateKey privateKey = state.getPrivateKey();


//        logger.debug("About to clean transaction pool...");
//
//        synchronized (pool.lock){
//            //cleanup transaction pool
//
//            hashes =  new ArrayList<>(transactionPool);
//
//            for (int i = 0; i < hashes.size(); i++){
//                String hash = hashes.get(i);
//
//                if (pool.checkExists(hash)){
//                    hashes.remove(i);
//                    transactionPool.remove(hash);
//                    i--;
//                }
//            }
//        }
//
//        logger.debug("Transaction pool cleaned!");

        Block block = AppBlockManager.instance().generateAndBroadcastBlock(hashes, privateKey, state);
        if (block != null) {
            size = BlockUtil.getTransactionsCount(block);

            long time = watch.time(TimeUnit.MILLISECONDS);
            long tps = (time > 0) ? ((size * 1000) / time) : 0;
            logger.info(" ###### Executed {} transactions in {}ms  TPS:{}   ###### ",
                    size, time, tps);
        }

//        Statistic stats = new Statistic();
//
//        stats.setNrTransactionsInBlock(size);
//        stats.setTps(tps);
//        stats.setCurrentTimeMillis(System.currentTimeMillis());
//
//        state.getStatisticsManager().addStatistic(stats);

        logger.traceExit();
    }

    private boolean isLeader(AppState state){
        ConsensusState consensusState = state.getConsensusState();
        String currentNodePeerID = AppShardingManager.instance().getCurrentPeerID(state);
        String selectedLeaderPeerID = consensusState.getSelectedLeaderPeerID();
        return ObjectUtil.isEqual(selectedLeaderPeerID, currentNodePeerID);
    }
}
