package network.elrond.chronology;

import network.elrond.Application;

public class SubRound {
    private RoundState roundState;
    private Round round;

    public SubRound(){
        round = null;
        roundState = null;
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

    @Override
    public String toString(){
        String strRoundState = "[NULL]";
        String strRound = "[NULL]";

        if (roundState != null){
            strRoundState = roundState.toString();
        }

        return (String.format("SubRound{RoundState=%s, round=%s}",
                roundState, round));
    }

}
