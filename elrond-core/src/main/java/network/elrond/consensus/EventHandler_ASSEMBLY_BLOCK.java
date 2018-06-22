package network.elrond.consensus;

import net.tomp2p.peers.Number160;
import network.elrond.Application;
import network.elrond.TimeWatch;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.chronology.SubRound;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.data.AppBlockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class EventHandler_ASSEMBLY_BLOCK implements EventHandler<SubRound, ArrayBlockingQueue<String>> {
    private static final Logger logger = LogManager.getLogger(EventHandler_ASSEMBLY_BLOCK.class);

    public void onEvent(Application application, Object sender, SubRound data, ArrayBlockingQueue<String> queue) {
        logger.traceEntry("params: {} {} {} {}", application, sender, data, queue);

        Util.check(application != null, "application is null while trying to get full nodes list!");
        Util.check(application.getState() != null, "state is null while trying to get full nodes list!");
        Util.check(application.getState().getConnection() != null, "connection is null while trying to get full nodes list!");

        String nodeName = application.getContext().getNodeName();

        if (!isThisNodesTurnToProcess(application.getState())) {
            logger.info("{}, round: {}, subRound: {}> Not this node's turn to process ...", nodeName, data.getRound().getIndex(), data.getRoundState().name());
            return;
        }

        AppState state = application.getState();
        for (int i = 0; i < 5; i++) {
            if (state.isLock()) {
                // If sync is running try to wait
                logger.info("{}, round: {}, subRound: {}> Can't execute, state locked! Retrying...", nodeName, data.getRound().getIndex(), data.getRoundState().name());
                ThreadUtil.sleep(50);
            }
        }

        if (state.isLock()) {
            logger.warn("{}, round: {}, subRound: {}> Can not acquire lock! Can not propose block!", nodeName, data.getRound().getIndex(), data.getRoundState().name());
            return;
        }

        if (state.getBlockchain().getCurrentBlock() == null) {
            // Require synchronize
            logger.info("{}, round: {}, subRound: {}> Can't execute, synchronize required!", nodeName, data.getRound().getIndex(), data.getRoundState().name());
            return;
        }

        int size = queue.size();
        TimeWatch watch = TimeWatch.start();

        state.setLock();
        proposeBlock(queue, application, data);
        state.clearLock();


        long time = watch.time(TimeUnit.MILLISECONDS);
        long tps = (time > 0) ? ((size*1000) / time) : 0;
        logger.info("{}, round: {}, subRound: {}> ###### Executed " + size + " transactions in " + time + "ms  TPS:" + tps + "   ###### ", nodeName,
                data.getRound().getIndex(), data.getRoundState().name());

        logger.traceExit();
    }

    private void proposeBlock(ArrayBlockingQueue<String> queue, Application application, SubRound data) {
        logger.traceEntry("params: {} {}", queue, application);

        AppState state = application.getState();

        List<String> hashes = new ArrayList<>(queue);
        queue.clear();

        String nodeName = application.getContext().getNodeName();

        if (hashes.isEmpty()) {
            logger.info("{}, round: {}, subRound: {}> Can't execute, no transaction!", nodeName,
                    data.getRound().getIndex(), data.getRoundState().name());
            return;
        }

        AppContext context = application.getContext();
        PrivateKey privateKey = context.getPrivateKey();

        AppBlockManager.instance().generateAndBroadcastBlock(hashes, privateKey, state);

        logger.traceExit();
    }

    private boolean isThisNodesTurnToProcess(AppState state){
        return(state.getConsensusStateHolder().getSelectedLeaderPeerID().equals(state.getConnection().getPeer().peerID()));
    }

}
