package network.elrond.chronology;

import java.util.EnumSet;

public enum RoundState {
    START_ROUND(0),
    SYNC_ROUND(0),
    PROPOSE_BLOCK(2500),
    END_ROUND(0);

    private final int roundStateDuration;

    private final static EnumSet<RoundState> MAIN_SET = EnumSet.allOf(RoundState.class);

    RoundState(final int roundStateDuration) {
        this.roundStateDuration = roundStateDuration;
    }

    public int getRoundStateDuration(){
        return (roundStateDuration);
    }

    @Override
    public String toString(){
        return (String.format("SubRoundType{%s, order=%d, duration:%d}", this.name(), this.ordinal(), this.roundStateDuration));
    }

    public static EnumSet<RoundState> getEnumSet(){
        return(MAIN_SET);
    }
}
