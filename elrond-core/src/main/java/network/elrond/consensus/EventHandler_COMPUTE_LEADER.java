package network.elrond.consensus;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import network.elrond.Application;
import network.elrond.chronology.SubRound;
import network.elrond.core.EventHandler;
import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;


public class EventHandler_COMPUTE_LEADER implements EventHandler<SubRound, ArrayBlockingQueue<String>> {
    private static final Logger logger = LogManager.getLogger(EventHandler_COMPUTE_LEADER.class);

    public void onEvent(Application application, Object sender, SubRound data, ArrayBlockingQueue<String> queue) {
        logger.traceEntry("params: {} {} {} {}", application, sender, data, queue);

        List<Number160> nodeList = getCurrentNodes(application);

        logger.debug("computed list as: {}", nodeList);

        application.getState().getConsensusStateHolder().setSelectedLeaderPeerID(computeLeader(nodeList, data.getRound().getIndex()));

        logger.debug("Round: {}, subRound: {} > current leader: {}", data.getRound().getIndex(), data.getRoundState(),
                application.getState().getConsensusStateHolder().getSelectedLeaderPeerID().toString());

        logger.traceExit();
    }

    public static List<Number160> getCurrentNodes(Application application){
        logger.traceEntry("params: ", application);

        Util.check(application != null, "application is null while trying to get full nodes list!");
        Util.check(application.getState() != null, "state is null while trying to get full nodes list!");
        Util.check(application.getState().getConnection() != null, "connection is null while trying to get full nodes list!");

        List<Number160> result = new ArrayList<>();

        logger.trace("added self");
        result.add(application.getState().getConnection().getPeer().peerID());

        for (PeerAddress peerAddress : application.getState().getConnection().getPeer().peerBean().peerMap().all()){
            result.add(peerAddress.peerId());
        }

        logger.trace("sort list so anyone will produce same list");
        Collections.sort(result);

        return logger.traceExit(result);
    }

    public Number160 computeLeader(List<Number160> nodeList, long roundIndex){
        int index = (int)(roundIndex % (long)nodeList.size());
        return(nodeList.get(index));
    }

}
