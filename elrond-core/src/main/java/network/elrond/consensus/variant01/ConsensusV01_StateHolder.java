package network.elrond.consensus.variant01;

import net.tomp2p.peers.Number160;

import java.util.concurrent.ArrayBlockingQueue;

public class ConsensusV01_StateHolder {
    private static ConsensusV01_StateHolder main = null;

    ArrayBlockingQueue<String> queueTransactionHashes = null;
    Number160 selectedLeaderPeerID;

    private ConsensusV01_StateHolder(){
        selectedLeaderPeerID = Number160.ZERO;
    }

    public static ConsensusV01_StateHolder instance(){
        if (main == null){
            main = new ConsensusV01_StateHolder();
        }

        return(main);
    }

    public Number160 getSelectedLeaderPeerID(){
        return(selectedLeaderPeerID);
    }

    public void setSelectedLeaderPeerID(Number160 selectedLeaderPeerID){
        this.selectedLeaderPeerID = selectedLeaderPeerID;
    }

}
