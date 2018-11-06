package network.elrond.chronology;

public class Round {
    private final long index;
    private final long startTimeStamp;

    public Round() {
        index = 0;
        startTimeStamp = 0;
    }

    public Round(long index, long startTimeStamp) {
        this.index = index;
        this.startTimeStamp = startTimeStamp;
    }

    public long getIndex() {
        return (index);
    }

    public long getStartTimeStamp() {
        return(startTimeStamp);
    }

    @Override
    public String toString(){
        return(String.format("Round{index=%d, start timestamp=%d}", index, startTimeStamp));
    }
}
