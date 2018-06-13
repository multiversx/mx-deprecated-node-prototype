package network.elrond.chronology;

import java.math.BigInteger;

public class Round {
    private long roundHeight;
    private boolean lastRoundInEpoch;

    public Round(){
        roundHeight = 0;
        lastRoundInEpoch = false;
    }

    public long getRoundHeight(){
        return (roundHeight);
    }

    public void setRoundHeight(long roundHeight){
        this.roundHeight = roundHeight;
    }

    public boolean isLastRoundInEpoch(){
        return(lastRoundInEpoch);
    }

    public void setLastRoundInEpoch(boolean lastRoundInEpoch){
        this.lastRoundInEpoch = lastRoundInEpoch;
    }
}
