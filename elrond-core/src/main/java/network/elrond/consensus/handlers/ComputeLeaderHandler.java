package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.chronology.SubRound;
import network.elrond.consensus.ConsensusState;
import network.elrond.core.EventHandler;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ComputeLeaderHandler implements EventHandler<SubRound> {
    private static final Logger logger = LogManager.getLogger(ComputeLeaderHandler.class);

    public void onEvent(AppState state, SubRound data) {
        logger.traceEntry("params: {} {}", state, data);

        Util.check(state != null, "application state is null");

        List<String> nodeList = AppShardingManager.instance().getPeersInBlock(state);

        logger.debug("Round: {}, subRound: {}> computed list as: {}", data.getRound().getIndex(), data.getRoundState().name(), nodeList);

        String computedLeaderPeerID = AppServiceProvider.getConsensusService().computeLeader(nodeList, data.getRound());
        ConsensusState consensusState = state.getConsensusState();
        consensusState.setSelectedLeaderPeerID(computedLeaderPeerID);

        logger.debug("Round: {}, subRound: {}> current leader: {}",
                data.getRound().getIndex(),
                data.getRoundState().name(),
                consensusState.getSelectedLeaderPeerID());

        logger.traceExit();
    }
}
