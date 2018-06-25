package network.elrond.consensus;

import net.tomp2p.peers.Number160;
import network.elrond.Application;
import network.elrond.chronology.SubRound;
import network.elrond.core.EventHandler;
import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;


public class EventHandler_START_ROUND implements EventHandler<SubRound, ArrayBlockingQueue<String>> {
    private static final Logger logger = LogManager.getLogger(EventHandler_START_ROUND.class);

    public void onEvent(Application application, Object sender, SubRound data, ArrayBlockingQueue<String> queue) {
        logger.traceEntry("params: {} {} {} {}", application, sender, data, queue);

        Util.check(application.getState() != null, "application state is null");

        String nodeName = application.getContext().getNodeName();

        application.getState().getConsensusStateHolder().setSelectedLeaderPeerID(Number160.ZERO);
        application.getState().getConsensusStateHolder().setStatisticsTransactionsProcessed(-1);
        logger.debug("{}, round: {}, subRound: {}> initialized!", nodeName, data.getRound().getIndex(), data.getRoundState().name());

        logger.traceExit();
    }



}
