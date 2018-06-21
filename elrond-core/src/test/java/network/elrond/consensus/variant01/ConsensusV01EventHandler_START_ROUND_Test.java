package network.elrond.consensus.variant01;

import junit.framework.TestCase;
import net.tomp2p.peers.Number160;
import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.chronology.Round;
import network.elrond.chronology.SubRound;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;

public class ConsensusV01EventHandler_START_ROUND_Test {

    @Test
    public void testIfStartRoundInitializesEverything(){
        TestCase.assertEquals(Number160.ZERO, ConsensusV01_StateHolder.instance().getSelectedLeaderPeerID());

        SubRound subRound = new SubRound();
        subRound.setRound(new Round());

        ConsensusV01_StateHolder.instance().setSelectedLeaderPeerID(Number160.ONE);
        TestCase.assertEquals(Number160.ONE, ConsensusV01_StateHolder.instance().getSelectedLeaderPeerID());

        ConsensusV01EventHandler_START_ROUND start_round = new ConsensusV01EventHandler_START_ROUND();
        Application application = new Application(new AppContext());
        start_round.onEvent(application, new Object(), subRound, new ArrayBlockingQueue<>(50000, true));

        TestCase.assertEquals(Number160.ZERO, ConsensusV01_StateHolder.instance().getSelectedLeaderPeerID());
    }


}
