package network.elrond.chronology;

public class SubRound {
	private final Round round;
    private final RoundState roundState;
    private final long timeStamp;
    
    public SubRound(Round round, RoundState roundState, long timeStamp) {
    	this.round = round;
		this.roundState = roundState;
		this.timeStamp = timeStamp;
	}

	public RoundState getRoundState(){
        return (roundState);
    }

    public Round getRound(){
        return(round);
    }

    public long getTimeStamp(){
        return(this.timeStamp);
    }

    @Override
    public String toString(){
        return (String.format("SubRound{RoundState=%s, round=%s, timestamp=%d}",
                roundState, round, timeStamp));
    }

}
