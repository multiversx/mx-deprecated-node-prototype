package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.chronology.SubRound;
import network.elrond.consensus.ConsensusState;
import network.elrond.core.EventHandler;
import network.elrond.core.Util;
import network.elrond.data.SyncState;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.AppShardingManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class StartRoundHandler implements EventHandler<SubRound> {
    private static final Logger logger = LogManager.getLogger(StartRoundHandler.class);

    public void onEvent(AppState state, SubRound data) {
        logger.traceEntry("params: {} {}", state, data);

        Util.check(state != null, "application state is null");

        ConsensusState consensusState = state.getConsensusState();
        consensusState.setSelectedLeaderPeerID(null);
        logger.debug("Round: {}, subRound: {}> initialized!", data.getRound().getIndex(), data.getRoundState().name());

        //compute and check sync required

        SyncState syncState = null;
        try {
            AppServiceProvider.getBootstrapService().fetchNetworkBlockIndex(state.getBlockchain());
            syncState = AppServiceProvider.getBootstrapService().getSyncState(state.getBlockchain());
            consensusState.setSyncReq(syncState.isSyncRequired());
        } catch (Exception ex){
            logger.catching(ex);
            consensusState.setSyncReq(true);
            return;
        }

        logger.debug("Round: {}, subround: {}> Sync req? {}, local: {}, network: {}", data.getRound().getIndex(), data.getRoundState().name(),
                consensusState.isSyncReq(), syncState.getLocalBlockIndex(), syncState.getRemoteBlockIndex());

        List<String> nodeList = AppShardingManager.instance().getPeersOnShard(state);

        logger.debug("Round: {}, subRound: {}> computed list as: {}", data.getRound().getIndex(), data.getRoundState().name(), nodeList);

        String computedLeaderPeerID = AppServiceProvider.getConsensusService().computeLeader(nodeList, data.getRound());
        consensusState.setSelectedLeaderPeerID(computedLeaderPeerID);

        logger.debug("Round: {}, subRound: {}> current leader: {}",
                data.getRound().getIndex(),
                data.getRoundState().name(),
                consensusState.getSelectedLeaderPeerID());

        logger.traceExit();
    }



}
