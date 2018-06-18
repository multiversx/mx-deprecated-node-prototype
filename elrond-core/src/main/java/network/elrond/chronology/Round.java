package network.elrond.chronology;

public class Round {
    private long index;
    private long startTimeStamp;

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

    public void setIndex(long index) {
        this.index = index;
    }

    public long getStartTimeStamp() {
        return(startTimeStamp);
    }

    public void setStartTimeStamp(long startTimeStamp){
        this.startTimeStamp = startTimeStamp;
    }

    @Override
    public String toString(){
        return(String.format("Round{index=%d, start timestamp=%d}", index, startTimeStamp));
    }
}
