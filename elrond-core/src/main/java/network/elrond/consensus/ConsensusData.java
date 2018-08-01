package network.elrond.consensus;

public class ConsensusData {
    String selectedLeaderPeerID;
    boolean syncReq = true;

    public ConsensusData(){
        selectedLeaderPeerID = "";
        syncReq = true;
    }

    public String getSelectedLeaderPeerID(){
        return selectedLeaderPeerID;
    }

    public void setSelectedLeaderPeerID(String selectedLeaderPeerID){
        this.selectedLeaderPeerID = selectedLeaderPeerID;
    }

    public boolean isSyncReq(){
        return (syncReq);
    }

    public void setSyncReq(boolean syncReq){
        this.syncReq = syncReq;
    }
}

