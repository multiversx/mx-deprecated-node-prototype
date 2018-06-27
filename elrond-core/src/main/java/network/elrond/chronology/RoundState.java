package network.elrond.chronology;

import network.elrond.consensus.handlers.AssemblyBlockHandler;
import network.elrond.consensus.handlers.NopHandler;
import network.elrond.consensus.handlers.StartRoundHandler;
import network.elrond.core.EventHandler;

import java.util.EnumSet;

public enum RoundState {
    START_ROUND(0, new StartRoundHandler()),
    PROPOSE_BLOCK(2500, new AssemblyBlockHandler()),
    END_ROUND(0, new NopHandler());


    private final int roundStateDuration;
    private final EventHandler eventHandler;

    private final static EnumSet<RoundState> MAIN_SET = EnumSet.allOf(RoundState.class);

    RoundState(final int roundStateDuration, final EventHandler eventHandler) {
        this.roundStateDuration = roundStateDuration;
        this.eventHandler = eventHandler;
    }

    public int getRoundStateDuration(){
        return (roundStateDuration);
    }

    public EventHandler getEventHandler() {
        return (eventHandler);
    }

    @Override
    public String toString(){
        return (String.format("SubRoundType{%s, order=%d, duration:%d}", this.name(), this.ordinal(), this.roundStateDuration));
    }

    public static EnumSet<RoundState> getEnumSet(){
        return(MAIN_SET);
    }
}
