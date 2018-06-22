package network.elrond.consensus;

import net.tomp2p.peers.Number160;
import network.elrond.Application;
import network.elrond.TimeWatch;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.chronology.SubRound;
import network.elrond.core.EventHandler;
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

        if (!isThisNodesTurnToProcess(application.getState())) {
            logger.info("Not this node's turn to process ...");
            return;
        }

        AppState state = application.getState();
        if (state.isLock()) {
            // If sync is running stop
            logger.info("Can't execute, state locked!");
            return;
        }

        if (state.getBlockchain().getCurrentBlock() == null) {
            // Require synchronize
            logger.info("Can't execute, synchronize required!");
            return;
        }

        int size = queue.size();
        TimeWatch watch = TimeWatch.start();

        state.setLock();
        proposeBlock(queue, application);
        state.clearLock();


        long time = watch.time(TimeUnit.MILLISECONDS);
        long tps = (time > 0) ? ((size*1000) / time) : 0;
        logger.info(" ###### Executed " + size + " transactions in " + time + "ms  TPS:" + tps + "   ###### ");

        logger.traceExit();
    }

    private void proposeBlock(ArrayBlockingQueue<String> queue, Application application) {
        logger.traceEntry("params: {} {}", queue, application);

        AppState state = application.getState();

        List<String> hashes = new ArrayList<>(queue);
        queue.clear();

        if (hashes.isEmpty()) {
            logger.info("Can't execute, no transaction!");
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
