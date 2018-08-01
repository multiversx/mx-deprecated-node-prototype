package network.elrond.consensus.handlers;

import network.elrond.TimeWatch;
import network.elrond.application.AppState;
import network.elrond.benchmark.Statistic;
import network.elrond.blockchain.TransactionsPool;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.RoundState;
import network.elrond.consensus.ConsensusData;
import network.elrond.core.EventHandler;
import network.elrond.core.ObjectUtil;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.AppBlockManager;
import network.elrond.data.Block;
import network.elrond.data.BlockUtil;
import network.elrond.data.BootstrapService;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AssemblyBlockHandler extends EventHandler {
    private static final Logger logger = LogManager.getLogger(AssemblyBlockHandler.class);

    public AssemblyBlockHandler(long currentRoundIndex){
        super(currentRoundIndex);
    }

    public EventHandler execute(AppState state, long genesisTimeStamp) {
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();
        BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();

        if (!chronologyService.isStillInRoundState(state.getNtpClient(), genesisTimeStamp, currentRoundIndex, RoundState.PROPOSE_BLOCK)){
            return new EndRoundHandler(currentRoundIndex);
        }

        ConsensusData consensusData = state.getConsensusData();

        if (!isLeader(state)) {
            logger.info("Round: {}, subRound: {}> Not this node's turn to process ...", currentRoundIndex, this.getClass().getName());

            return new EndRoundHandler(currentRoundIndex);
        }

        logger.info("Round: {}, subRound: {}> This node will assemble block.", currentRoundIndex, this.getClass().getName());

        proposeBlock(state);

        return new EndRoundHandler(currentRoundIndex);
    }


    private void proposeBlock(AppState state) {
        logger.traceEntry("params: {}", state);

        TransactionsPool pool = state.getPool();
        List<String> hashes = pool.getTransactions();
        if (hashes.isEmpty()) {
            logger.info("Round: {}, subRound: {}> Can't execute, no transactions!",
                    currentRoundIndex, this.getClass().getName());
            state.getStatisticsManager().addStatistic(new Statistic(0));
            return;
        }

        TimeWatch watch = TimeWatch.start();
        int size = 0;

        PrivateKey privateKey = state.getPrivateKey();

        Block block = AppBlockManager.instance().generateAndBroadcastBlock(hashes, privateKey, state);
        if (block != null) {
            size = BlockUtil.getTransactionsCount(block);

            long time = watch.time(TimeUnit.MILLISECONDS);
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
