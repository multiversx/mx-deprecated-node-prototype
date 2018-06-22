package network.elrond.consensus;

import net.tomp2p.peers.Number160;

import java.util.concurrent.ArrayBlockingQueue;

public class ConsensusStateHolder {
    Number160 selectedLeaderPeerID;

    public ConsensusStateHolder(){

        selectedLeaderPeerID = Number160.ZERO;
    }

    public Number160 getSelectedLeaderPeerID(){
        return(selectedLeaderPeerID);
    }

    public void setSelectedLeaderPeerID(Number160 selectedLeaderPeerID){
        this.selectedLeaderPeerID = selectedLeaderPeerID;
    }

}
