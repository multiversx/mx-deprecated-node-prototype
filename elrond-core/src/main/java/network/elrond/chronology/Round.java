package network.elrond.chronology;

import java.math.BigInteger;

public class Round {
    private long index;
    private long startRoundMillis;

    public Round() {
        index = 0;
        startRoundMillis = 0;
    }

    public Round(long index, long startRoundMillis) {
        this.index = index;
        this.startRoundMillis = startRoundMillis;
    }

    public long getIndex() {
        return (index);
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getStartRoundMillis() {
        return(startRoundMillis);
    }

    public void setStartRoundMillis(long startRoundMillis){
        this.startRoundMillis = startRoundMillis;
    }

    @Override
    public String toString(){
        return(String.format("Round{index=%d, start timestamp=%d}", index, startRoundMillis));
    }
}
