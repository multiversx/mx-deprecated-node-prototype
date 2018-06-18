package network.elrond.chronology;

import java.util.*;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

public enum RoundState {
    START_ROUND(0),
    PROPOSE_BLOCK(2000),
    VERIFY_BLOCK(1000),
    MULTI_SIGN_ROUND_1(250),
    MULTI_SIGN_ROUND_2(250),
    MULTI_SIGN_ROUND_3(250),
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
