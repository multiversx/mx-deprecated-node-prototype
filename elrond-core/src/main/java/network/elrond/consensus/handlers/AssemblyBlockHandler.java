package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.benchmark.Statistic;
import network.elrond.blockchain.TransactionsPool;
import network.elrond.chronology.SubRound;
import network.elrond.consensus.ConsensusData;
import network.elrond.core.EventHandler;
import network.elrond.core.ObjectUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.AppBlockManager;
import network.elrond.data.BlockUtil;
import network.elrond.data.model.Block;
import network.elrond.sharding.AppShardingManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AssemblyBlockHandler implements EventHandler<SubRound> {
    private static final Logger logger = LogManager.getLogger(AssemblyBlockHandler.class);

    @Override
	public void onEvent(AppState state, SubRound data) {
        logger.traceEntry("params: {} {}",state, data);

        Util.check(state != null, "state is null while trying to get full nodes list!");

        ConsensusData consensusData = state.getConsensusData();

        if (consensusData.isSyncReq()){
            logger.info("Round: {}, subRound: {}> Sync req ...", data.getRound().getIndex(), data.getRoundState().name());
            logger.traceExit();
            return;
        }

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

        Stopwatch stopwatch = Stopwatch.createStarted();
        int size = 0;

        PrivateKey privateKey = state.getPrivateKey();

        Block block = AppBlockManager.instance().generateAndBroadcastBlock(hashes, privateKey, state);
        if (block != null) {
            size = BlockUtil.getTransactionsCount(block);

            long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            long tps = (time > 0) ? ((size * 1000) / time) : 0;
            logger.info(" ###### Executed {} transactions in {}ms  TPS:{}   ###### ",
                    size, time, tps);
        }

        logger.traceExit();
    }

    private boolean isLeader(AppState state){
        ConsensusData consensusData = state.getConsensusData();
        String currentNodePeerID = AppShardingManager.instance().getCurrentPeerID(state);
        String selectedLeaderPeerID = consensusData.getSelectedLeaderPeerID();
        return ObjectUtil.isEqual(selectedLeaderPeerID, currentNodePeerID);
    }
}
