package network.elrond.consensus;

public class ConsensusState {
    String selectedLeaderPeerID;

    public ConsensusState(){
        selectedLeaderPeerID = "";
    }

    public String getSelectedLeaderPeerID(){
        return selectedLeaderPeerID;
    }

    public void setSelectedLeaderPeerID(String selectedLeaderPeerID){
        this.selectedLeaderPeerID = selectedLeaderPeerID;
    }
}
