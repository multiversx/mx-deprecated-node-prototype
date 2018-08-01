package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.Round;
import network.elrond.consensus.ConsensusData;
import network.elrond.core.EventHandler;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class StartRoundHandler extends EventHandler {
    private static final Logger logger = LogManager.getLogger(StartRoundHandler.class);

    public StartRoundHandler(long currentRoundIndex)  {
        super(currentRoundIndex);
    }

    public EventHandler execute(AppState state, long genesisTimeStamp) {
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();

        long currentTimeStamp = chronologyService.getSynchronizedTime(state.getNtpClient());

        Round currentRound = AppServiceProvider.getChronologyService().getRoundFromDateTime(genesisTimeStamp, currentTimeStamp);
        currentRoundIndex = currentRound.getIndex();

        ConsensusData consensusData = state.getConsensusData();
        consensusData.setSelectedLeaderPeerID(null);
        logger.debug("Round: {}, subRound: {}> initialized!", currentRoundIndex, this.getClass().getName());

        List<String> nodeList = AppShardingManager.instance().getPeersOnShardInBlock(state);

        logger.debug("Round: {}, subRound: {}> computed list as: {}", currentRound.getIndex(), this.getClass().getName(), nodeList);

        String computedLeaderPeerID = AppServiceProvider.getConsensusService().computeLeader(nodeList, currentRound);
        consensusData.setSelectedLeaderPeerID(computedLeaderPeerID);

        logger.debug("Round: {}, subRound: {}> current leader: {}",
                currentRound.getIndex(), this.getClass().getName(),
                consensusData.getSelectedLeaderPeerID());

        return new SyncRoundHandler(currentRoundIndex);
    }

    public long getCurrentRoundIndex(){
        return currentRoundIndex;
    }
}
