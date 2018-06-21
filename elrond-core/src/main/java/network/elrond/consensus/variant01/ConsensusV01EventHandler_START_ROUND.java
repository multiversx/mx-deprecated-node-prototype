package network.elrond.consensus.variant01;

import net.tomp2p.peers.Number160;
import network.elrond.Application;
import network.elrond.chronology.ChronologyServiceImpl;
import network.elrond.chronology.SubRound;
import network.elrond.chronology.SubRoundEventHandler;
import network.elrond.core.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;


public class ConsensusV01EventHandler_START_ROUND implements EventHandler<SubRound, ArrayBlockingQueue<String>> {
    private static final Logger logger = LogManager.getLogger(ConsensusV01EventHandler_START_ROUND.class);

    public void onEvent(Application application, Object sender, SubRound data, ArrayBlockingQueue<String> queue) {
        logger.traceEntry("params: {} {} {} {}", application, sender, data, queue);

        ConsensusV01_StateHolder.instance().setSelectedLeaderPeerID(Number160.ZERO);
        logger.debug("Round: {}, subRound: {} > initialized!", data.getRound().getIndex(), data.getRoundState());

        logger.traceExit();
    }



}
