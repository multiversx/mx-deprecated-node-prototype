package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.chronology.SubRound;
import network.elrond.consensus.ConsensusState;
import network.elrond.core.EventHandler;
import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StartRoundHandler implements EventHandler<SubRound> {
    private static final Logger logger = LogManager.getLogger(StartRoundHandler.class);

    public void onEvent(AppState state, SubRound data) {
        logger.traceEntry("params: {} {}", state, data);

        Util.check(state != null, "application state is null");

        ConsensusState consensusState = state.getConsensusState();
        consensusState.setSelectedLeaderPeerID(null);
        logger.debug("Round: {}, subRound: {}> initialized!", data.getRound().getIndex(), data.getRoundState().name());

        logger.traceExit();
    }



}
