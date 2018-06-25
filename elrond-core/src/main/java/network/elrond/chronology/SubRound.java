package network.elrond.chronology;

public class SubRound {
    private RoundState roundState;
    private Round round;
    private long timeStamp;

    public SubRound(){
        round = null;
        roundState = null;
        timeStamp = 0;
    }

    public RoundState getRoundState(){
        return (roundState);
    }

    public void setRoundState(RoundState roundState){
        this.roundState = roundState;
    }

    public Round getRound(){
        return(round);
    }

    public void setRound(Round round){
        this.round = round;
    }

    public void setTimeStamp(long timeStamp){
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp(){
        return(this.timeStamp);
    }

    @Override
    public String toString(){
        String strRoundState = "[NULL]";
        String strRound = "[NULL]";

        if (roundState != null){
            strRoundState = roundState.toString();
        }

        return (String.format("SubRound{RoundState=%s, round=%s, timestamp=%d}",
                roundState, round, timeStamp));
    }

}
