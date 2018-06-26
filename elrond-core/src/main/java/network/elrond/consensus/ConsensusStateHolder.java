package network.elrond.consensus;

import net.tomp2p.peers.Number160;
import network.elrond.chronology.RoundState;

public class ConsensusStateHolder {
    Number160 selectedLeaderPeerID;
    RoundState currentRoundState;
    long currentRoundIndex;
    long statisticsTransactionsProcessed;
    public String nodeName;

    public ConsensusStateHolder(){
        selectedLeaderPeerID = Number160.ZERO;
        currentRoundIndex = 0;
        currentRoundState = null;
        statisticsTransactionsProcessed = -1;
        nodeName = "";
    }

    public Number160 getSelectedLeaderPeerID(){
        return selectedLeaderPeerID;
    }

    public void setSelectedLeaderPeerID(Number160 selectedLeaderPeerID){
        this.selectedLeaderPeerID = selectedLeaderPeerID;
    }

    public long getCurrentRoundIndex(){
        return (currentRoundIndex);
    }

    public void setCurrentRoundIndex(long roundIndex){
        this.currentRoundIndex = roundIndex;
    }

    public RoundState getCurrentRoundState(){
        return (currentRoundState);
    }

    public void setCurrentRoundState(RoundState roundState){
        this.currentRoundState = roundState;
    }

    public long getStatisticsTransactionsProcessed(){
        return (statisticsTransactionsProcessed);
    }

    public void setStatisticsTransactionsProcessed(long transactionsProcessed){
        this.statisticsTransactionsProcessed = transactionsProcessed;
    }
}
